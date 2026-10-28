package com.securebank.service;

import com.securebank.entity.AuditLog;
import com.securebank.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Manages the immutable audit trail. */
@Service
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepo;

    public AuditService(AuditLogRepository auditLogRepo) {
        this.auditLogRepo = auditLogRepo;
    }

    /** Append an audit entry. riskScore may be null for non-transaction events. */
    public AuditLog log(String actor, String action, String details, Integer riskScore) {
        AuditLog entry = new AuditLog();
        entry.setId("log_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 5));
        entry.setTimestamp(Instant.now());
        entry.setActor(actor);
        entry.setAction(action);
        entry.setDetails(details);
        entry.setRiskScore(riskScore);
        return auditLogRepo.save(entry);
    }

    /** Overload without riskScore (system events). */
    public AuditLog log(String actor, String action, String details) {
        return log(actor, action, details, null);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAll() {
        return auditLogRepo.findAllByOrderByTimestampDesc();
    }
}
