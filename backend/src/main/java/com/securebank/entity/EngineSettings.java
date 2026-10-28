package com.securebank.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores fraud engine weight and threshold settings, persisted in the DB.
 * Only one row (id=1) is used — the singleton pattern.
 */
@Entity
@Table(name = "engine_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EngineSettings {

    @Id
    private Long id = 1L;

    // ── Weight factors ───────────────────────────────────────────
    private int weightAmountDeviation   = 30;
    private int weightAbsoluteHighAmount = 20;
    private int weightOddHours          = 15;
    private int weightUnusualCity       = 20;
    private int weightUnknownDevice     = 20;
    private int weightVelocityBurst     = 35;

    // ── Threshold factors ────────────────────────────────────────
    private int thresholdMediumRiskMin  = 41;
    private int thresholdHighRiskMin    = 71;
}
