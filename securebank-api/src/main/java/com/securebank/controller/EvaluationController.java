package com.securebank.controller;

import com.securebank.entity.Transaction;
import com.securebank.entity.EngineSettings;
import com.securebank.repository.EngineSettingsRepository;
import com.securebank.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Provides confusion matrix and performance metrics for the Evaluation page.
 * Also exposes CRUD for engine settings (weights and thresholds).
 */
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final TransactionRepository    txRepo;
    private final EngineSettingsRepository settingsRepo;

    public EvaluationController(TransactionRepository txRepo,
                                 EngineSettingsRepository settingsRepo) {
        this.txRepo       = txRepo;
        this.settingsRepo = settingsRepo;
    }

    /**
     * GET /api/evaluation/metrics?medThreshold=41&highThreshold=71
     * Returns confusion matrix, score distribution, and ML metrics
     * computed over all simulated transactions.
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(
            @RequestParam(defaultValue = "41") int medThreshold,
            @RequestParam(defaultValue = "71") int highThreshold) {

        List<Transaction> simulated = txRepo.findBySimulatedTrue();

        int tp = 0, fp = 0, tn = 0, fn = 0;
        int[] scoreBins = new int[5];
        int approved = 0, review = 0, blocked = 0;

        for (Transaction tx : simulated) {
            boolean isActualThreat    = "FRAUD".equals(tx.getTrueLabel()) || "SUSPICIOUS".equals(tx.getTrueLabel());
            boolean isPredictedThreat = tx.getRiskScore() >= medThreshold;

            if      (isActualThreat  && isPredictedThreat)  tp++;
            else if (!isActualThreat && isPredictedThreat)  fp++;
            else if (!isActualThreat && !isPredictedThreat) tn++;
            else                                            fn++;

            int binIdx = Math.min(4, (int)(tx.getRiskScore() / 20.001));
            scoreBins[binIdx]++;

            if      (tx.getRiskScore() >= highThreshold) blocked++;
            else if (tx.getRiskScore() >= medThreshold)  review++;
            else                                         approved++;
        }

        int total = simulated.isEmpty() ? 1 : simulated.size();
        double accuracy  = (double)(tp + tn) / total * 100;
        double precision = (tp + fp) > 0 ? (double)tp / (tp + fp) * 100 : 0;
        double recall    = (tp + fn) > 0 ? (double)tp / (tp + fn) * 100 : 0;
        double f1        = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0;

        return ResponseEntity.ok(Map.of(
            "totalSimulated", simulated.size(),
            "confusionMatrix", Map.of("tp", tp, "fp", fp, "tn", tn, "fn", fn),
            "metrics", Map.of(
                "accuracy",  Math.round(accuracy  * 10) / 10.0,
                "precision", Math.round(precision * 10) / 10.0,
                "recall",    Math.round(recall    * 10) / 10.0,
                "f1",        Math.round(f1        * 10) / 10.0
            ),
            "scoreBins",  List.of(scoreBins[0], scoreBins[1], scoreBins[2], scoreBins[3], scoreBins[4]),
            "outcomes", Map.of("approved", approved, "review", review, "blocked", blocked)
        ));
    }

    /** GET /api/evaluation/settings — get current engine weights */
    @GetMapping("/settings")
    public ResponseEntity<EngineSettings> getSettings() {
        return ResponseEntity.ok(settingsRepo.findById(1L).orElse(new EngineSettings()));
    }

    /** PUT /api/evaluation/settings — update engine weights */
    @PutMapping("/settings")
    public ResponseEntity<EngineSettings> updateSettings(@RequestBody EngineSettings updated) {
        updated.setId(1L); // enforce singleton
        return ResponseEntity.ok(settingsRepo.save(updated));
    }
}
