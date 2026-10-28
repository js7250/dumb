package com.securebank.service;

import com.securebank.dto.TransactionDto;
import com.securebank.entity.*;
import com.securebank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Generates realistic synthetic transactions for admin simulation testing.
 * Mirrors the JS simulatorService / pickProfile logic.
 */
@Service
@RequiredArgsConstructor
public class SimulatorService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final FraudEngineService fraudEngine;
    private final EntityMapper mapper;

    // Random data pools
    private static final List<String> NORMAL_PAYEES = List.of(
            "Zomato Merchant Services", "Amazon India", "Airtel Broadband HQ",
            "Swiggy Food Delivery", "HDFC Home Loan EMI", "Reliance JioFiber",
            "Ola Rides", "Flipkart Pvt. Ltd.", "Netflix India", "Spotify Premium");

    private static final List<String> FRAUD_PAYEES = List.of(
            "Unknown Overseas Merchant", "Crypto Exchange X99", "Anonymous Wire Transfer",
            "Offshore Holding Co.", "ShadowPay Systems", "VPN Shield Payments",
            "QuickCash Terminal", "Dark Market Escrow", "Anon Payment Gateway");

    private static final List<String> CITIES = List.of(
            "Mumbai", "New Delhi", "Bangalore", "Hyderabad", "Chennai",
            "Kolkata", "Pune", "Ahmedabad", "Surat", "Jaipur");

    private static final List<String> DEVICES = List.of(
            "Chrome on MacBook Pro (Work)", "iPhone 15 Mobile App",
            "Samsung Galaxy S24 App", "Firefox on Windows 11",
            "Unknown Linux Terminal", "Tor Browser", "Unrecognized Device");

    private static final Random RANDOM = new Random();

    @Transactional
    public TransactionDto generateOne(String profileType) {
        // Use the admin user as owner of simulated transactions
        User admin = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        Instant timestamp = Instant.now();
        double amount;
        String payee;
        String city;
        String device;
        String trueLabel;

        switch (profileType.toUpperCase()) {
            case "FRAUD" -> {
                amount    = 50_000 + RANDOM.nextDouble() * 950_000;   // ₹50k – ₹10L
                payee     = pick(FRAUD_PAYEES);
                city      = pick(CITIES);   // always different
                device    = DEVICES.get(DEVICES.size() - 1 - RANDOM.nextInt(2)); // unrecognized
                // Simulate night hour for extra score
                timestamp = timestamp.minusSeconds((long) (3 * 3600 + RANDOM.nextInt(3600)));
                trueLabel = "FRAUD";
            }
            case "SUSPICIOUS" -> {
                amount    = 15_000 + RANDOM.nextDouble() * 85_000;    // ₹15k – ₹1L
                payee     = RANDOM.nextBoolean() ? pick(NORMAL_PAYEES) : pick(FRAUD_PAYEES);
                city      = RANDOM.nextBoolean() ? admin.getCity() : pick(CITIES);
                device    = RANDOM.nextBoolean() ? pick(admin.getKnownDevices()) : pick(DEVICES);
                trueLabel = "SUSPICIOUS";
            }
            default -> {   // NORMAL
                amount    = 500 + RANDOM.nextDouble() * 9_500;         // ₹500 – ₹10k
                payee     = pick(NORMAL_PAYEES);
                city      = admin.getCity();
                device    = pick(admin.getKnownDevices().isEmpty()
                        ? List.of("Chrome on MacBook Pro (Work)") : admin.getKnownDevices());
                trueLabel = "NORMAL";
            }
        }

        FraudEngineService.FraudResult result =
                fraudEngine.evaluate(amount, city, device, timestamp, admin);

        Transaction tx = Transaction.builder()
                .user(admin)
                .accountNumber(admin.getAccountNumber())
                .type("Transfer")
                .amount(amount)
                .payee(payee)
                .city(city)
                .device(device)
                .timestamp(timestamp)
                .riskScore(result.riskScore())
                .riskBand(result.riskBand())
                .reasons(result.reasons())
                .status(result.status())
                .isSimulated(true)
                .trueLabel(trueLabel)
                .build();

        transactionRepository.save(tx);
        return mapper.toTransactionDto(tx);
    }

    @Transactional
    public void purgeSimulated() {
        List<Transaction> simulated = transactionRepository.findByIsSimulatedTrue();
        transactionRepository.deleteAll(simulated);
    }

    private <T> T pick(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
