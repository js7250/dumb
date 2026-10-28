package com.securebank.init;

import com.securebank.entity.*;
import com.securebank.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Seeds the H2 database with demo accounts, transactions, audit log, and engine settings
 * on first startup. Idempotent — skips if data already exists.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository        userRepo;
    private final TransactionRepository txRepo;
    private final AuditLogRepository    auditRepo;
    private final EngineSettingsRepository settingsRepo;
    private final PasswordEncoder       encoder;

    public DataInitializer(UserRepository userRepo, TransactionRepository txRepo,
                            AuditLogRepository auditRepo,
                            EngineSettingsRepository settingsRepo,
                            PasswordEncoder encoder) {
        this.userRepo     = userRepo;
        this.txRepo       = txRepo;
        this.auditRepo    = auditRepo;
        this.settingsRepo = settingsRepo;
        this.encoder      = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return; // Already seeded

        // ── Seed Users ────────────────────────────────────────────
        User jai = new User();
        jai.setId("usr_jai_01");
        jai.setUsername("jai");
        jai.setPassword(encoder.encode("user123"));
        jai.setRole("USER");
        jai.setFullName("Jai Singh");
        jai.setAccountNumber("50100492812345");
        jai.setAccountType("Savings Account");
        jai.setBalance(125350.0);
        jai.setFrozen(false);
        jai.setCity("Mumbai");
        jai.setKnownDevices(List.of("Chrome on MacBook Pro (Work)", "iPhone 15 Mobile App"));
        jai.setAverageTxAmount(4200.0);
        userRepo.save(jai);

        User admin = new User();
        admin.setId("usr_admin_01");
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("admin123"));
        admin.setRole("ADMIN");
        admin.setFullName("Security Officer, Mr.White");
        admin.setAccountNumber("10000000000001");
        admin.setAccountType("Treasury Admin");
        admin.setBalance(9_999_999.0);
        admin.setFrozen(false);
        admin.setCity("New Delhi");
        admin.setKnownDevices(List.of("Corporate Sentinel Workstation"));
        admin.setAverageTxAmount(50000.0);
        userRepo.save(admin);

        // ── Seed Transactions ─────────────────────────────────────
        Transaction t1 = new Transaction();
        t1.setId("tx_seed_01");
        t1.setUserId("usr_jai_01");
        t1.setAccountNumber("50100492812345");
        t1.setType("Transfer");
        t1.setAmount(3200.0);
        t1.setPayee("Zomato Merchant Services");
        t1.setCity("Mumbai");
        t1.setDevice("iPhone 15 Mobile App");
        t1.setTimestamp(Instant.now().minusSeconds(86400L * 2));
        t1.setRiskScore(8);
        t1.setRiskBand("LOW");
        t1.setReasons(List.of("Normal transaction within daily profile"));
        t1.setStatus("COMPLETED");
        t1.setSimulated(false);
        t1.setTrueLabel("NORMAL");
        txRepo.save(t1);

        Transaction t2 = new Transaction();
        t2.setId("tx_seed_02");
        t2.setUserId("usr_jai_01");
        t2.setAccountNumber("50100492812345");
        t2.setType("Transfer");
        t2.setAmount(12000.0);
        t2.setPayee("Airtel Broadband HQ");
        t2.setCity("Mumbai");
        t2.setDevice("Chrome on MacBook Pro (Work)");
        t2.setTimestamp(Instant.now().minusSeconds(86400L * 1));
        t2.setRiskScore(18);
        t2.setRiskBand("LOW");
        t2.setReasons(List.of("Expected recurring utility vendor"));
        t2.setStatus("COMPLETED");
        t2.setSimulated(false);
        t2.setTrueLabel("NORMAL");
        txRepo.save(t2);

        // ── Seed Engine Settings (singleton) ─────────────────────
        if (settingsRepo.count() == 0) {
            settingsRepo.save(new EngineSettings());
        }

        // ── Seed Audit Log ────────────────────────────────────────
        AuditLog bootstrap = new AuditLog();
        bootstrap.setId("log_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 5));
        bootstrap.setTimestamp(Instant.now());
        bootstrap.setActor("SYSTEM");
        bootstrap.setAction("SYSTEM_BOOTSTRAP");
        bootstrap.setDetails("Initialized SecureBank database with seed accounts: jai (USER), admin (ADMIN).");
        bootstrap.setRiskScore(null);
        auditRepo.save(bootstrap);

        System.out.println("[DataInitializer] ✅ Seed data loaded successfully.");
    }
}
