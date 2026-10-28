package com.securebank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** Payload sent by the React frontend when creating a new transaction. */
public record CreateTransactionRequest(
        @NotBlank String type,
        @Positive double amount,
        @NotBlank String payee,
        @NotBlank String city,
        @NotBlank String device
) {}
