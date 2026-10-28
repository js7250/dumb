package com.securebank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_user", columnList = "user_id"),
        @Index(name = "idx_tx_status", columnList = "status"),
        @Index(name = "idx_tx_timestamp", columnList = "timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 30)
    private String type;       // Transfer, Payment, Deposit, Withdraw

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 100)
    private String payee;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 200)
    private String device;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    // ── Fraud engine output ──────────────────────────────────────
    @Column(nullable = false)
    private Integer riskScore;

    @Column(nullable = false, length = 10)
    private String riskBand;   // LOW, MEDIUM, HIGH

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "transaction_reasons", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "reason", length = 300)
    @Builder.Default
    private List<String> reasons = new ArrayList<>();

    @Column(nullable = false, length = 30)
    private String status;     // COMPLETED, PENDING_REVIEW, BLOCKED, RESOLVED_SAFE

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSimulated = false;

    @Column(length = 15)
    private String trueLabel;  // NORMAL, SUSPICIOUS, FRAUD — only for simulated
}
