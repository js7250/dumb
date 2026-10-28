package com.securebank.controller;

import com.securebank.dto.CreateTransactionRequest;
import com.securebank.dto.UpdateStatusRequest;
import com.securebank.dto.UserDTO;
import com.securebank.entity.Transaction;
import com.securebank.repository.UserRepository;
import com.securebank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService txService;
    private final UserRepository     userRepo;

    public TransactionController(TransactionService txService, UserRepository userRepo) {
        this.txService = txService;
        this.userRepo  = userRepo;
    }

    /** GET /api/transactions — returns all txs for ADMIN, own txs for USER */
    @GetMapping
    public ResponseEntity<List<Transaction>> getAll(@AuthenticationPrincipal UserDetails principal) {
        var user = userRepo.findByUsernameIgnoreCase(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(txService.getTransactionsForUser(user.getId(), user.getRole()));
    }

    /** POST /api/transactions — create and evaluate a new transaction */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateTransactionRequest req,
                                    @AuthenticationPrincipal UserDetails principal) {
        try {
            var user = userRepo.findByUsernameIgnoreCase(principal.getUsername()).orElseThrow();
            Transaction tx = txService.createTransaction(
                user.getId(), req.type(), req.amount(), req.payee(), req.city(), req.device()
            );

            String message = switch (tx.getStatus()) {
                case "BLOCKED"        -> "Transaction BLOCKED due to critical fraud anomaly. Account has been protected & frozen.";
                case "PENDING_REVIEW" -> "Transaction flagged for high-vigilance review. Security audit logged.";
                default               -> "Transaction approved and settled securely.";
            };

            // Return tx + updated user info + message
            var freshUser = userRepo.findById(user.getId()).orElseThrow();
            return ResponseEntity.ok(Map.of(
                "transaction", tx,
                "message", message,
                "accountWasFrozen", "HIGH".equals(tx.getRiskBand()),
                "user", UserDTO.from(freshUser)
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** PATCH /api/transactions/{id}/status — admin override transaction status */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Transaction> updateStatus(@PathVariable String id,
                                                     @Valid @RequestBody UpdateStatusRequest req,
                                                     @AuthenticationPrincipal UserDetails principal) {
        Transaction tx = txService.updateStatus(id, req.status(), principal.getUsername());
        return ResponseEntity.ok(tx);
    }

    /** GET /api/transactions/flagged — flagged (MEDIUM/HIGH) transactions */
    @GetMapping("/flagged")
    public ResponseEntity<List<Transaction>> getFlagged(@AuthenticationPrincipal UserDetails principal) {
        var user = userRepo.findByUsernameIgnoreCase(principal.getUsername()).orElseThrow();
        if ("ADMIN".equals(user.getRole())) {
            return ResponseEntity.ok(txService.getFlaggedTransactions());
        }
        // Regular user only sees their own flagged txs
        return ResponseEntity.ok(
            txService.getTransactionsForUser(user.getId(), "USER").stream()
                .filter(t -> "HIGH".equals(t.getRiskBand()) || "MEDIUM".equals(t.getRiskBand())
                          || "BLOCKED".equals(t.getStatus()))
                .toList()
        );
    }
}
