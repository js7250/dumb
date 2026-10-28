package com.securebank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Returned after a transaction is created — includes freeze notification. */
@Data
@AllArgsConstructor
public class TransactionResponse {
    private TransactionDto transaction;
    private String message;
    private boolean accountWasFrozen;
    private UserDto user;
}
