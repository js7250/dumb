package com.securebank.repository;

import com.securebank.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    /** All audit logs, newest first */
    List<AuditLog> findAllByOrderByTimestampDesc();
}
