package com.securebank.dto;

import jakarta.validation.constraints.NotBlank;

/** Used by admin to override a transaction status. */
public record UpdateStatusRequest(@NotBlank String status) {}
