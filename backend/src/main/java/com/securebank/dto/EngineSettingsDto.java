package com.securebank.dto;

import lombok.Data;

/** Fraud engine weight and threshold settings DTO (mirrors EngineSettings entity). */
@Data
public class EngineSettingsDto {
    private int weightAmountDeviation;
    private int weightAbsoluteHighAmount;
    private int weightOddHours;
    private int weightUnusualCity;
    private int weightUnknownDevice;
    private int weightVelocityBurst;
    private int thresholdMediumRiskMin;
    private int thresholdHighRiskMin;
}
