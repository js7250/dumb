package com.securebank.service;

import com.securebank.dto.FraudResult;
import com.securebank.entity.Transaction;
import com.securebank.entity.User;
import com.securebank.repository.TransactionRepository;
import com.securebank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Generates synthetic transactions for stress-testing the fraud engine.
 * Mirrors simulatorService.js logic.
 */
@Service
@Transactional
public class SimulatorService {

    private static final List<String> INDIAN_CITIES = List.of(
        "Mumbai", "Delhi NCR", "Bengaluru", "Hyderabad",
        "Chennai", "Kolkata", "Pune", "Ahmedabad", "Jaipur"
    );
    private static final List<String> KNOWN_DEVICES = List.of(
        "Chrome on MacBook Pro (Work)", "iPhone 15 Mobile App"
    );
    private static final List<String> FRAUD_DEVICES = List.of(
        "Tor Exit Node / Linux Firefox",
        "Unrooted Android Emulator v11",
        "Remote Desktop Client (RDP)"
    );
    private static final List<String> PAYEES = List.of(
        "Reliance Retail Ltd", "Tata Power Utilities",
        "Flipkart India Pvt", "Crypto Bridge Global LLC", "Offshore Escrow Ltd"
    );

    private final UserRepository        userRepo;
    private final TransactionRepository txRepo;
    private final FraudEngineService    fraudEngine;
    private final AuditService          auditService;

    private final Random random = new Random();

    public SimulatorService(UserRepository userRepo, TransactionRepository txRepo,
                             FraudEngineService fraudEngine, AuditService auditService) {
        this.userRepo     = userRepo;
        this.txRepo       = txRepo;
        this.fraudEngine  = fraudEngine;
        this.auditService = auditService;
    }

    /** Generate a single synthetic transaction for the given profile type. */
    public Transaction generateSingle(String profileType) {
        List<User> users = userRepo.findAll().stream()
                .filter(u -> "USER".equals(u.getRole())).toList();
        User user = users.isEmpty() ? null : users.get(random.nextInt(users.size()));
        if (user == null) throw new IllegalStateException("No USER accounts found for simulation.");

        double amount;
        String city;
        String device;
        String payee;
        Instant timestamp = Instant.now();

        switch (profileType) {
            case "NORMAL" -> {
                amount = random.nextInt(3500) + 250.0;
                city   = user.getCity();
                device = KNOWN_DEVICES.get(random.nextInt(KNOWN_DEVICES.size()));
                payee  = PAYEES.get(random.nextInt(3));
            }
            case "SUSPICIOUS" -> {
                amount = random.nextInt(25000) + 12000.0;
                city   = INDIAN_CITIES.stream()
                             .filter(c -> !c.equalsIgnoreCase(user.getCity()))
                             .findFirst().orElse("Ahmedabad");
                device = KNOWN_DEVICES.get(0);
                payee  = PAYEES.get(random.nextInt(PAYEES.size()));
            }
            default -> { // FRAUD
                amount = random.nextInt(200000) + 85000.0;
                city   = "Kyiv / Overseas Proxy";
                device = FRAUD_DEVICES.get(random.nextInt(FRAUD_DEVICES.size()));
                payee  = "Crypto Bridge Global LLC";
                // Set to 02:35 AM to trigger temporal rule
                timestamp = LocalDate.now()
                    .atTime(2, 35, 12)
                    .atZone(ZoneId.of("Asia/Kolkata"))
                    .toInstant();
            }
        }

        FraudResult eval = fraudEngine.evaluate(amount, city, device, timestamp, user);

        Transaction tx = new Transaction();
        tx.setId("sim_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4));
        tx.setUserId(user.getId());
        tx.setAccountNumber(user.getAccountNumber());
        tx.setType("Transfer");
        tx.setAmount(amount);
        tx.setPayee(payee);
        tx.setCity(city);
        tx.setDevice(device);
        tx.setTimestamp(timestamp);
        tx.setRiskScore(eval.riskScore());
        tx.setRiskBand(eval.riskBand());
        tx.setReasons(eval.reasons());
        tx.setStatus(eval.status());
        tx.setSimulated(true);
        tx.setTrueLabel(profileType);
        txRepo.save(tx);

        return tx;
    }

    /** Generate a full batch based on percentage mix. Returns all generated records. */
    public List<Transaction> generateBatch(int totalCount, double normalPct,
                                            double suspiciousPct, double fraudPct) {
        List<Transaction> results = new ArrayList<>();
        for (int i = 0; i < totalCount; i++) {
            double rand = random.nextDouble() * 100;
            String profile;
            if (rand < normalPct) profile = "NORMAL";
            else if (rand < normalPct + suspiciousPct) profile = "SUSPICIOUS";
            else profile = "FRAUD";
            results.add(generateSingle(profile));
        }
        return results;
    }

    /** Delete all simulated transactions and log the purge. */
    public void purgeSimulatedData(String adminUsername) {
        txRepo.deleteBySimulatedTrue();
        auditService.log("ADMIN_SIMULATOR", "PURGE_SIMULATION_DATA",
            "Purged all synthetic transactions (isSimulated=true) by " + adminUsername + ".");
    }
}
