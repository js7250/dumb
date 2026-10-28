package com.securebank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_actor", columnList = "actor"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String actor;       // username or "SYSTEM"

    @Column(nullable = false, length = 50)
    private String action;      // LOGIN_SUCCESS, TRANSACTION_CREATED, FREEZE_TOGGLED, etc.

    @Column(nullable = false, length = 500)
    private String details;

    @Column
    private Integer riskScore;  // nullable

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant timestamp;
}
