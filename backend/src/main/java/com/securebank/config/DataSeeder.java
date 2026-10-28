package com.securebank.config;

import com.securebank.entity.*;
import com.securebank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * DataSeeder — runs on every startup and inserts demo data if the DB is empty.
 *
 * Demo accounts (matching the JS version):
 *   Customer : jai   / user123
 *   Admin    : admin / admin123
 *
 * Also seeds the default EngineSettings singleton and a bootstrap audit log.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final EngineSettingsRepository engineSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedEngineSettings();
        seedBootstrapAuditLog();
    }

    private void seedUsers() {
        if (userRepository.existsByUsername("jai")) {
            log.info("DataSeeder: Users already seeded — skipping.");
            return;
        }

        User jai = User.builder()
                .username("jai")
                .password(passwordEncoder.encode("user123"))
                .role(User.Role.USER)
                .fullName("Jai Singh")
                .accountNumber("50100492812345")
                .accountType("Savings Account")
                .balance(125350.0)
                .isFrozen(false)
                .city("Mumbai")
                .knownDevices(List.of("Chrome on MacBook Pro (Work)", "iPhone 15 Mobile App"))
                .averageTxAmount(4200.0)
                .build();

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN)
                .fullName("Security Officer, Mr.White")
                .accountNumber("10000000000001")
                .accountType("Treasury Admin")
                .balance(9999999.0)
                .isFrozen(false)
                .city("New Delhi")
                .knownDevices(List.of("Corporate Sentinel Workstation"))
                .averageTxAmount(50000.0)
                .build();

        userRepository.saveAll(List.of(jai, admin));

        // Seed two sample transactions for jai
        log.info("DataSeeder: Seeded 2 demo users (jai, admin).");
    }

    private void seedEngineSettings() {
        if (engineSettingsRepository.existsById(1L)) return;
        engineSettingsRepository.save(new EngineSettings());
        log.info("DataSeeder: Engine settings initialized with defaults.");
    }

    private void seedBootstrapAuditLog() {
        if (auditLogRepository.count() > 0) return;
        auditLogRepository.save(AuditLog.builder()
                .actor("SYSTEM")
                .action("SYSTEM_BOOTSTRAP")
                .details("SecureBank Spring Boot backend initialized. MySQL schema created. Demo accounts seeded.")
                .riskScore(0)
                .build());
        log.info("DataSeeder: Bootstrap audit log created.");
    }
}
