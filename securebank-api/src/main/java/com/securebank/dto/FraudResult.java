package com.securebank.dto;

import java.util.List;

/** Result from the fraud engine evaluation for a single transaction. */
public record FraudResult(
        int riskScore,
        String riskBand,
        String status,
        List<String> reasons
) {}
