package com.securebank.service;

import com.securebank.dto.FraudResult;
import com.securebank.entity.Transaction;
import com.securebank.entity.User;
import com.securebank.repository.TransactionRepository;
import com.securebank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Core transaction processing with fraud evaluation and balance management. */
@Service
@Transactional
public class TransactionService {

    private static final List<String> OUTFLOW_TYPES = List.of("Transfer", "Payment", "Withdraw");

    private final TransactionRepository txRepo;
    private final UserRepository        userRepo;
    private final FraudEngineService    fraudEngine;
    private final AuditService          auditService;

    public TransactionService(TransactionRepository txRepo, UserRepository userRepo,
                               FraudEngineService fraudEngine, AuditService auditService) {
        this.txRepo       = txRepo;
        this.userRepo     = userRepo;
        this.fraudEngine  = fraudEngine;
        this.auditService = auditService;
    }

    /**
     * Create and process a transaction through the fraud engine.
     * Balance is only adjusted for COMPLETED (approved) transactions.
     */
    public Transaction createTransaction(String userId, String type, double amount,
                                          String payee, String city, String device) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User record not found."));

        if (amount <= 0) {
            throw new IllegalArgumentException("Please enter a valid positive transfer amount.");
        }

        // CHECK 1: Account frozen
        if (user.isFrozen()) {
            auditService.log(user.getUsername(), "BLOCKED_FROZEN_ATTEMPT",
                String.format("Transaction of ₹%.2f blocked because account %s is FROZEN.",
                    amount, user.getAccountNumber()), 90);
            throw new IllegalStateException(
                "SECURITY ALERT: Your account is currently FROZEN due to risk enforcement. Transactions are disabled.");
        }

        // CHECK 2: Sufficient balance for outflows
        boolean isOutflow = OUTFLOW_TYPES.contains(type);
        if (isOutflow && user.getBalance() < amount) {
            throw new IllegalArgumentException(
                String.format("Insufficient funds. Available balance: ₹%.2f", user.getBalance()));
        }

        // EVALUATE via fraud engine
        Instant now = Instant.now();
        FraudResult eval = fraudEngine.evaluate(amount, city, device, now, user);

        // Auto-freeze on HIGH risk
        if ("HIGH".equals(eval.riskBand())) {
            user.setFrozen(true);
            userRepo.save(user);
            auditService.log("FRAUD_ENGINE_DAEMON", "ACCOUNT_FROZEN_AUTOMATICALLY",
                String.format("Account %s frozen. Risk score: %d/100.", user.getAccountNumber(), eval.riskScore()),
                eval.riskScore());
        }

        // Apply balance ONLY if fully COMPLETED (not PENDING_REVIEW or BLOCKED)
        if ("COMPLETED".equals(eval.status())) {
            if (isOutflow) {
                user.setBalance(user.getBalance() - amount);
                // Update rolling average on settled outflows only
                List<Transaction> completedOutflows = txRepo.findByUserId(userId).stream()
                        .filter(t -> "COMPLETED".equals(t.getStatus()) && OUTFLOW_TYPES.contains(t.getType()))
                        .toList();
                double newAvg = (user.getAverageTxAmount() * completedOutflows.size() + amount)
                                / (completedOutflows.size() + 1);
                user.setAverageTxAmount(newAvg);
            } else if ("Deposit".equals(type)) {
                user.setBalance(user.getBalance() + amount);
            }
            userRepo.save(user);
        }

        // Persist transaction record
        Transaction tx = new Transaction();
        tx.setId("tx_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4));
        tx.setUserId(userId);
        tx.setAccountNumber(user.getAccountNumber());
        tx.setType(type);
        tx.setAmount(amount);
        tx.setPayee(payee != null ? payee : "Self Cashier");
        tx.setCity(city);
        tx.setDevice(device);
        tx.setTimestamp(now);
        tx.setRiskScore(eval.riskScore());
        tx.setRiskBand(eval.riskBand());
        tx.setReasons(eval.reasons());
        tx.setStatus(eval.status());
        tx.setSimulated(false);
        tx.setTrueLabel("NORMAL");
        txRepo.save(tx);

        // Audit the transaction
        auditService.log(user.getUsername(),
            "TRANSACTION_" + eval.status(),
            String.format("%s of ₹%.2f to \"%s\". Risk: %d (%s).",
                type, amount, tx.getPayee(), eval.riskScore(), eval.riskBand()),
            eval.riskScore());

        return tx;
    }

    /** Admin: override transaction status (e.g., mark as COMPLETED after review). */
    public Transaction updateStatus(String txId, String newStatus, String adminUsername) {
        Transaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
        String oldStatus = tx.getStatus();
        tx.setStatus(newStatus);
        txRepo.save(tx);

        auditService.log(adminUsername, "TRANSACTION_OVERRIDE",
            String.format("Admin changed tx #%s status from %s to %s.",
                txId.substring(Math.max(0, txId.length() - 6)), oldStatus, newStatus),
            tx.getRiskScore());

        return tx;
    }

    /** Admin: toggle account freeze/unfreeze. */
    public User toggleFreeze(String userId, String adminUsername) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setFrozen(!user.isFrozen());
        userRepo.save(user);

        auditService.log(adminUsername,
            user.isFrozen() ? "MANUAL_FREEZE" : "MANUAL_UNFREEZE",
            String.format("Account %s (%s) set to %s.",
                user.getAccountNumber(), user.getFullName(), user.isFrozen() ? "FROZEN" : "ACTIVE"));
        return user;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsForUser(String userId, String role) {
        if ("ADMIN".equals(role)) {
            return txRepo.findAllByOrderByTimestampDesc();
        }
        return txRepo.findByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getFlaggedTransactions() {
        return txRepo.findFlaggedTransactions();
    }
}
