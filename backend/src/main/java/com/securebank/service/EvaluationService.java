package com.securebank.service;

import com.securebank.dto.EngineSettingsDto;
import com.securebank.dto.EvaluationMetricsDto;
import com.securebank.entity.EngineSettings;
import com.securebank.entity.Transaction;
import com.securebank.repository.EngineSettingsRepository;
import com.securebank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final TransactionRepository transactionRepository;
    private final EngineSettingsRepository settingsRepository;
    private final EntityMapper mapper;

    /**
     * Compute confusion matrix over all simulated transactions.
     * Ground truth  = trueLabel (FRAUD/SUSPICIOUS/NORMAL)
     * Predicted     = riskScore vs provided thresholds
     *
     * TP = truly FRAUD/SUSPICIOUS and engine predicted HIGH/MEDIUM
     * FP = truly NORMAL but engine predicted HIGH/MEDIUM
     * FN = truly FRAUD/SUSPICIOUS but engine predicted LOW
     * TN = truly NORMAL and engine predicted LOW
     */
    public EvaluationMetricsDto getMetrics(int medThreshold, int highThreshold) {
        List<Transaction> simulated = transactionRepository.findByIsSimulatedTrue();

        int tp = 0, fp = 0, tn = 0, fn = 0;

        for (Transaction tx : simulated) {
            boolean actuallyFraud   = "FRAUD".equals(tx.getTrueLabel()) || "SUSPICIOUS".equals(tx.getTrueLabel());
            boolean predictedFraud  = tx.getRiskScore() >= medThreshold;

            if (actuallyFraud  && predictedFraud)  tp++;
            else if (!actuallyFraud && predictedFraud)  fp++;
            else if (actuallyFraud  && !predictedFraud) fn++;
            else                                         tn++;
        }

        double precision = (tp + fp) > 0 ? (double) tp / (tp + fp) : 0;
        double recall    = (tp + fn) > 0 ? (double) tp / (tp + fn) : 0;
        double f1        = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0;
        double accuracy  = simulated.isEmpty() ? 0 : (double) (tp + tn) / simulated.size();

        return EvaluationMetricsDto.builder()
                .truePositives(tp)
                .falsePositives(fp)
                .trueNegatives(tn)
                .falseNegatives(fn)
                .precision(round2(precision))
                .recall(round2(recall))
                .f1Score(round2(f1))
                .accuracy(round2(accuracy))
                .totalSimulated(simulated.size())
                .medThreshold(medThreshold)
                .highThreshold(highThreshold)
                .build();
    }

    public EngineSettingsDto getSettings() {
        EngineSettings settings = settingsRepository.findById(1L).orElseGet(EngineSettings::new);
        return mapper.toEngineSettingsDto(settings);
    }

    @Transactional
    public EngineSettingsDto updateSettings(EngineSettingsDto dto) {
        EngineSettings settings = settingsRepository.findById(1L).orElseGet(EngineSettings::new);
        mapper.applyEngineSettingsDto(dto, settings);
        settingsRepository.save(settings);
        return mapper.toEngineSettingsDto(settings);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
