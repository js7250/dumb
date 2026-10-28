package com.securebank.entity;

import com.securebank.util.StringListConverter;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a financial transaction with embedded fraud evaluation results.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_user_id", columnList = "userId"),
    @Index(name = "idx_tx_timestamp", columnList = "timestamp")
})
public class Transaction {

    @Id
    @Column(length = 60)
    private String id;

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    /** Transfer | Payment | Deposit | Withdraw */
    @Column(nullable = false, length = 20)
    private String type;

    private double amount;

    @Column(nullable = false)
    private String payee;

    @Column(length = 100)
    private String city;

    @Column(length = 200)
    private String device;

    @Column(nullable = false)
    private Instant timestamp;

    private int riskScore;

    /** LOW | MEDIUM | HIGH */
    @Column(length = 10)
    private String riskBand;

    /** Fraud engine decision reasons (stored as JSON array) */
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> reasons = new ArrayList<>();

    /** COMPLETED | PENDING_REVIEW | BLOCKED */
    @Column(length = 20)
    private String status;

    private boolean simulated;

    /** NORMAL | SUSPICIOUS | FRAUD (ground-truth for simulator evaluation) */
    @Column(length = 20)
    private String trueLabel;

    // ── Constructors ─────────────────────────────────────────────
    public Transaction() {}

    // ── Getters & Setters ────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPayee() { return payee; }
    public void setPayee(String payee) { this.payee = payee; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskBand() { return riskBand; }
    public void setRiskBand(String riskBand) { this.riskBand = riskBand; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isSimulated() { return simulated; }
    public void setSimulated(boolean simulated) { this.simulated = simulated; }

    public String getTrueLabel() { return trueLabel; }
    public void setTrueLabel(String trueLabel) { this.trueLabel = trueLabel; }
}
