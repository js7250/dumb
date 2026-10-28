package com.securebank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateTransactionRequest {

    @NotBlank
    private String type;      // Transfer, Payment, Deposit, Withdraw

    @Positive
    @NotNull
    private Double amount;

    @NotBlank
    @Size(max = 100)
    private String payee;

    @NotBlank
    @Size(max = 50)
    private String city;

    @NotBlank
    @Size(max = 200)
    private String device;
}
