package com.securebank.entity;

import jakarta.persistence.*;

/**
 * Singleton entity that stores fraud engine rule weights and risk thresholds.
 * Always fetched with id=1L. Created automatically by DataInitializer on startup.
 */
@Entity
@Table(name = "engine_settings")
public class EngineSettings {

    /** Always 1 — singleton row */
    @Id
    private Long id = 1L;

    // ── Rule Weights (0–100) ──────────────────────────────────────
    private int weightAmountDeviation   = 30;
    private int weightAbsoluteHighAmount = 20;
    private int weightOddHours          = 15;
    private int weightUnusualCity       = 20;
    private int weightUnknownDevice     = 20;
    private int weightVelocityBurst     = 35;

    // ── Risk Thresholds ───────────────────────────────────────────
    private int mediumRiskMin = 41;
    private int highRiskMin   = 71;

    // ── Constructors ─────────────────────────────────────────────
    public EngineSettings() {}

    // ── Getters & Setters ────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getWeightAmountDeviation() { return weightAmountDeviation; }
    public void setWeightAmountDeviation(int v) { this.weightAmountDeviation = v; }

    public int getWeightAbsoluteHighAmount() { return weightAbsoluteHighAmount; }
    public void setWeightAbsoluteHighAmount(int v) { this.weightAbsoluteHighAmount = v; }

    public int getWeightOddHours() { return weightOddHours; }
    public void setWeightOddHours(int v) { this.weightOddHours = v; }

    public int getWeightUnusualCity() { return weightUnusualCity; }
    public void setWeightUnusualCity(int v) { this.weightUnusualCity = v; }

    public int getWeightUnknownDevice() { return weightUnknownDevice; }
    public void setWeightUnknownDevice(int v) { this.weightUnknownDevice = v; }

    public int getWeightVelocityBurst() { return weightVelocityBurst; }
    public void setWeightVelocityBurst(int v) { this.weightVelocityBurst = v; }

    public int getMediumRiskMin() { return mediumRiskMin; }
    public void setMediumRiskMin(int v) { this.mediumRiskMin = v; }

    public int getHighRiskMin() { return highRiskMin; }
    public void setHighRiskMin(int v) { this.highRiskMin = v; }
}
