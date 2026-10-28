package com.securebank.controller;

import com.securebank.dto.*;
import com.securebank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /** GET /api/transactions — user sees own, admin sees all */
    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAll(Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);
        return ResponseEntity.ok(transactionService.getTransactions(authentication.getName(), isAdmin));
    }

    /** GET /api/transactions/flagged */
    @GetMapping("/flagged")
    public ResponseEntity<List<TransactionDto>> getFlagged(Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);
        return ResponseEntity.ok(transactionService.getFlagged(authentication.getName(), isAdmin));
    }

    /** POST /api/transactions — create a new transaction */
    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication authentication) {
        TransactionResponse response = transactionService.createTransaction(request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /** PATCH /api/transactions/{id}/status — admin updates status */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TransactionDto> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(transactionService.updateStatus(id, newStatus, authentication.getName()));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
