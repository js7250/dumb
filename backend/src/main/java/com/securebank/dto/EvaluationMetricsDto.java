package com.securebank.dto;

import lombok.Builder;
import lombok.Data;

/** Confusion matrix + precision/recall metrics returned by EvaluationController. */
@Data
@Builder
public class EvaluationMetricsDto {
    private int truePositives;
    private int falsePositives;
    private int trueNegatives;
    private int falseNegatives;
    private double precision;
    private double recall;
    private double f1Score;
    private double accuracy;
    private int totalSimulated;
    private int medThreshold;
    private int highThreshold;
}
