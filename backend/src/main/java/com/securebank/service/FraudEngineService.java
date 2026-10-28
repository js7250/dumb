package com.securebank.service;

import com.securebank.entity.EngineSettings;
import com.securebank.entity.Transaction;
import com.securebank.entity.User;
import com.securebank.repository.EngineSettingsRepository;
import com.securebank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Java port of the JavaScript fraudEngine.js heuristic rule engine.
 * Rules:
 *  1. Amount deviation from historical baseline
 *  2. Absolute high-value outflow (>= ₹1,00,000)
 *  3. Temporal anomaly (01:00 – 05:00 AM IST)
 *  4. Geographic mismatch (unknown city)
 *  5. Unknown device
 *  6. Velocity burst (≥ 2 transactions in 10 minutes)
 */
@Service
@RequiredArgsConstructor
public class FraudEngineService {

    private final EngineSettingsRepository settingsRepository;
    private final TransactionRepository transactionRepository;

    public record FraudResult(int riskScore, String riskBand, String status, List<String> reasons) {}

    /**
     * Evaluate a new transaction payload against the user profile.
     *
     * @param amount       Transaction amount
     * @param city         City of transaction
     * @param device       Device string
     * @param txTime       Exact transaction timestamp
     * @param user         Owner's profile (city, known devices, average amount)
     * @return             FraudResult with score, band, status, and reason list
     */
    public FraudResult evaluate(double amount, String city, String device,
                                Instant txTime, User user) {
        EngineSettings s = getSettings();
        int score = 0;
        List<String> reasons = new ArrayList<>();
        double avg = user.getAverageTxAmount() != null ? user.getAverageTxAmount() : 4000.0;

        // ── Rule 1: Amount deviation ──────────────────────────────
        if (amount > avg * 6) {
            score += s.getWeightAmountDeviation();
            reasons.add(String.format("Extreme amount anomaly: Transaction amount is >6x typical average (₹%.0f)", avg));
        } else if (amount > avg * 3) {
            score += Math.round(s.getWeightAmountDeviation() * 0.6f);
            reasons.add("Moderate amount deviation: >3x typical user average");
        }

        // ── Rule 2: Absolute high value ───────────────────────────
        if (amount >= 100_000) {
            score += s.getWeightAbsoluteHighAmount();
            reasons.add("High single-ticket transfer exceeding ₹1,00,000 threshold");
        }

        // ── Rule 3: Temporal anomaly (IST night hours) ────────────
        int hour = txTime.atZone(ZoneId.of("Asia/Kolkata")).getHour();
        if (hour >= 1 && hour <= 5) {
            score += s.getWeightOddHours();
            reasons.add("Off-hours operation: Originating between 01:00 AM and 05:00 AM IST");
        }

        // ── Rule 4: Geographic mismatch ───────────────────────────
        String baseCity = user.getCity() != null ? user.getCity().toLowerCase() : "";
        String txCity   = city != null ? city.toLowerCase() : "";
        if (!baseCity.isEmpty() && !txCity.isEmpty() && !baseCity.equals(txCity)) {
            score += s.getWeightUnusualCity();
            reasons.add(String.format("Geographical mismatch: Initiated from %s, registered base is %s", city, user.getCity()));
        }

        // ── Rule 5: Unknown device ────────────────────────────────
        boolean knownDevice = user.getKnownDevices().stream()
                .anyMatch(d -> d.equalsIgnoreCase(device != null ? device : ""));
        if (!knownDevice) {
            score += s.getWeightUnknownDevice();
            reasons.add("Unrecognized client signature: " + (device != null ? device : "Unidentified Hardware"));
        }

        // ── Rule 6: Velocity burst (10-minute window) ─────────────
        Instant windowStart = txTime.minusSeconds(600);
        List<Transaction> recentTxs = transactionRepository.findByUserAndTimestampBetween(user, windowStart, txTime);
        int recentCount = recentTxs.size();
        if (recentCount >= 4) {
            score += s.getWeightVelocityBurst();
            reasons.add(String.format("Velocity alert: %d high-frequency transactions logged in a 10-minute window", recentCount));
        } else if (recentCount >= 2) {
            score += Math.round(s.getWeightVelocityBurst() * 0.5f);
            reasons.add("Elevated velocity: Rapid repeat transactions within 10 minutes");
        }

        // ── Cap and classify ──────────────────────────────────────
        int finalScore = Math.min(100, Math.max(0, score));

        String riskBand = "LOW";
        String status   = "COMPLETED";
        if (finalScore >= s.getThresholdHighRiskMin()) {
            riskBand = "HIGH";
            status   = "BLOCKED";
        } else if (finalScore >= s.getThresholdMediumRiskMin()) {
            riskBand = "MEDIUM";
            status   = "PENDING_REVIEW";
        }

        if (reasons.isEmpty()) {
            reasons.add("Transaction conforms with baseline biometric and behavioral patterns");
        }

        return new FraudResult(finalScore, riskBand, status, reasons);
    }

    private EngineSettings getSettings() {
        return settingsRepository.findById(1L).orElseGet(EngineSettings::new);
    }
}
