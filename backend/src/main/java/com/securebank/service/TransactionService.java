package com.securebank.service;

import com.securebank.dto.*;
import com.securebank.entity.*;
import com.securebank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final FraudEngineService fraudEngine;
    private final EntityMapper mapper;

    /**
     * Create a real (user-initiated) transaction.
     * Runs fraud evaluation, optionally auto-freezes account on HIGH risk.
     */
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest req, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getIsFrozen()) {
            throw new IllegalStateException("Your account is temporarily frozen. All outgoing transfers are blocked.");
        }

        Instant now = Instant.now();
        FraudEngineService.FraudResult result =
                fraudEngine.evaluate(req.getAmount(), req.getCity(), req.getDevice(), now, user);

        Transaction tx = Transaction.builder()
                .user(user)
                .accountNumber(user.getAccountNumber())
                .type(req.getType())
                .amount(req.getAmount())
                .payee(req.getPayee())
                .city(req.getCity())
                .device(req.getDevice())
                .timestamp(now)
                .riskScore(result.riskScore())
                .riskBand(result.riskBand())
                .reasons(result.reasons())
                .status(result.status())
                .isSimulated(false)
                .build();

        transactionRepository.save(tx);

        // Deduct balance only on COMPLETED
        if ("COMPLETED".equals(result.status())) {
            if (List.of("Transfer", "Payment", "Withdraw").contains(req.getType())) {
                user.setBalance(user.getBalance() - req.getAmount());
            } else {
                user.setBalance(user.getBalance() + req.getAmount());
            }
        }

        // Auto-freeze on HIGH risk
        boolean wasFrozen = false;
        if ("HIGH".equals(result.riskBand()) && !user.getIsFrozen()) {
            user.setIsFrozen(true);
            wasFrozen = true;
            auditLogRepository.save(AuditLog.builder()
                    .actor("SYSTEM")
                    .action("AUTO_FREEZE")
                    .details(String.format(
                            "Account %s auto-frozen by Fraud Sentinel. Risk score: %d. Trigger transaction: %s",
                            user.getAccountNumber(), result.riskScore(), tx.getId()))
                    .riskScore(result.riskScore())
                    .build());
            log.warn("Auto-freeze triggered for user {} | score={}", username, result.riskScore());
        }

        userRepository.save(user);

        auditLogRepository.save(AuditLog.builder()
                .actor(username)
                .action("TRANSACTION_CREATED")
                .details(String.format("Tx %s | %s ₹%.2f to %s | Score: %d | %s",
                        tx.getId(), req.getType(), req.getAmount(), req.getPayee(),
                        result.riskScore(), result.status()))
                .riskScore(result.riskScore())
                .build());

        String message = wasFrozen
                ? "Transaction blocked — account auto-frozen due to high-risk activity."
                : "Transaction processed successfully.";

        return new TransactionResponse(mapper.toTransactionDto(tx), message, wasFrozen, mapper.toUserDto(user));
    }

    /** Get all transactions for a user (USER role) or all transactions (ADMIN). */
    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactions(String username, boolean isAdmin) {
        if (isAdmin) {
            return transactionRepository.findAllByOrderByTimestampDesc()
                    .stream().map(mapper::toTransactionDto).toList();
        }
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return transactionRepository.findByUserOrderByTimestampDesc(user)
                .stream().map(mapper::toTransactionDto).toList();
    }

    /** Get flagged (MEDIUM/HIGH) transactions */
    @Transactional(readOnly = true)
    public List<TransactionDto> getFlagged(String username, boolean isAdmin) {
        Long userId = null;
        if (!isAdmin) {
            User user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            userId = user.getId();
        }
        return transactionRepository.findFlagged(userId)
                .stream().map(mapper::toTransactionDto).toList();
    }

    /** Update a transaction's status (admin action — mark safe, etc.) */
    @Transactional
    public TransactionDto updateStatus(String txId, String newStatus, String adminUsername) {
        Transaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + txId));
        tx.setStatus(newStatus);
        transactionRepository.save(tx);

        auditLogRepository.save(AuditLog.builder()
                .actor(adminUsername)
                .action("TRANSACTION_STATUS_UPDATED")
                .details(String.format("Tx %s status changed to %s by admin %s", txId, newStatus, adminUsername))
                .build());

        return mapper.toTransactionDto(tx);
    }
}
