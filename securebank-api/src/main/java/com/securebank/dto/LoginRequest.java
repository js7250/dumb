package com.securebank.dto;

import jakarta.validation.constraints.NotBlank;

/** Login request body. */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
