package com.securebank.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/** Request body for the synthetic transaction simulator. */
public record SimulatorRequest(
        @Positive @Max(300) int totalCount,
        @Min(0) @Max(100) double normalPct,
        @Min(0) @Max(100) double suspiciousPct,
        @Min(0) @Max(100) double fraudPct
) {}
