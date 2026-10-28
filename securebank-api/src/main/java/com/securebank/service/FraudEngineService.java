package com.securebank.service;

import com.securebank.dto.FraudResult;
import com.securebank.entity.EngineSettings;
import com.securebank.entity.Transaction;
import com.securebank.entity.User;
import com.securebank.repository.EngineSettingsRepository;
import com.securebank.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Heuristic fraud detection engine — Java translation of fraudEngine.js.
 *
 * Rules evaluated:
 *  1. Amount deviation from user's historical baseline
 *  2. Absolute high-value transfer (&gt;= ₹1,00,000)
 *  3. Temporal anomaly (01:00–05:00 AM IST)
 *  4. Geographic mismatch (city vs. registered base)
 *  5. Unrecognized device fingerprint
 *  6. Velocity burst (rapid repeat transactions)
 */
@Service
public class FraudEngineService {

    private final EngineSettingsRepository settingsRepo;
    private final TransactionRepository    transactionRepo;

    public FraudEngineService(EngineSettingsRepository settingsRepo,
                               TransactionRepository transactionRepo) {
        this.settingsRepo    = settingsRepo;
        this.transactionRepo = transactionRepo;
    }

    /**
     * Evaluate a pending transaction against heuristic rules.
     *
     * @param amount    Transaction amount in INR
     * @param city      Originating city
     * @param device    Client device fingerprint
     * @param timestamp Transaction instant (null = now)
     * @param user      The transacting user (for baseline comparison)
     * @return          FraudResult with score, band, status, and human-readable reasons
     */
    public FraudResult evaluate(double amount, String city, String device,
                                Instant timestamp, User user) {

        EngineSettings s = settingsRepo.findById(1L).orElse(new EngineSettings());

        int score = 0;
        List<String> reasons = new ArrayList<>();

        double avg = user.getAverageTxAmount() > 0 ? user.getAverageTxAmount() : 4000.0;

        // ── Rule 1: Amount deviation from baseline ────────────────
        if (amount > avg * 6) {
            score += s.getWeightAmountDeviation();
            reasons.add(String.format(
                "Extreme amount anomaly: Transaction amount is >6x typical average (₹%.0f)", avg));
        } else if (amount > avg * 3) {
            score += Math.round(s.getWeightAmountDeviation() * 0.6f);
            reasons.add("Moderate amount deviation: >3x typical user average");
        }

        // ── Rule 2: Absolute high-value transfer ──────────────────
        if (amount >= 100_000) {
            score += s.getWeightAbsoluteHighAmount();
            reasons.add("High single-ticket transfer exceeding ₹1,00,000 threshold");
        }

        // ── Rule 3: Temporal anomaly (odd night hours) ────────────
        Instant txInstant = timestamp != null ? timestamp : Instant.now();
        int hour = LocalDateTime.ofInstant(txInstant, ZoneId.of("Asia/Kolkata")).getHour();
        if (hour >= 1 && hour <= 5) {
            score += s.getWeightOddHours();
            reasons.add("Off-hours operation: Originating between 01:00 AM and 05:00 AM IST");
        }

        // ── Rule 4: Geographic mismatch ───────────────────────────
        String baseCity = user.getCity() != null ? user.getCity().toLowerCase() : "";
        String txCity   = city != null ? city.toLowerCase() : "";
        if (!baseCity.isEmpty() && !txCity.isEmpty() && !baseCity.equals(txCity)) {
            score += s.getWeightUnusualCity();
            reasons.add(String.format(
                "Geographical mismatch: Initiated from %s, registered base is %s", city, user.getCity()));
        }

        // ── Rule 5: Device reputation ─────────────────────────────
        boolean knownDevice = user.getKnownDevices().stream()
                .anyMatch(d -> d.equalsIgnoreCase(device));
        if (!knownDevice) {
            score += s.getWeightUnknownDevice();
            reasons.add("Unrecognized client signature: " +
                (device != null ? device : "Unidentified Hardware"));
        }

        // ── Rule 6: Velocity burst (last 10 minutes) ──────────────
        long tenMinMs = 10L * 60 * 1000;
        List<Transaction> pastTxs = transactionRepo.findByUserId(user.getId());
        long recentCount = pastTxs.stream()
                .filter(t -> Math.abs(txInstant.toEpochMilli() - t.getTimestamp().toEpochMilli()) <= tenMinMs
                          && t.getTimestamp().isBefore(txInstant))   // only PAST transactions
                .count();

        if (recentCount >= 4) {
            score += s.getWeightVelocityBurst();
            reasons.add(String.format(
                "Velocity alert: %d high-frequency transactions logged in a 10-minute window", recentCount));
        } else if (recentCount >= 2) {
            score += Math.round(s.getWeightVelocityBurst() * 0.5f);
            reasons.add("Elevated velocity: Rapid repeat transactions within 10 minutes");
        }

        // ── Finalize ─────────────────────────────────────────────
        int finalScore = Math.min(100, Math.max(0, score));

        String riskBand, status;
        if (finalScore >= s.getHighRiskMin()) {
            riskBand = "HIGH"; status = "BLOCKED";
        } else if (finalScore >= s.getMediumRiskMin()) {
            riskBand = "MEDIUM"; status = "PENDING_REVIEW";
        } else {
            riskBand = "LOW"; status = "COMPLETED";
        }

        if (reasons.isEmpty()) {
            reasons.add("Transaction conforms with baseline biometric and behavioral patterns");
        }

        return new FraudResult(finalScore, riskBand, status, reasons);
    }
}
