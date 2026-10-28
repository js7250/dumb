package com.securebank.controller;

import com.securebank.dto.EngineSettingsDto;
import com.securebank.dto.EvaluationMetricsDto;
import com.securebank.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluation")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /** GET /api/evaluation/metrics?medThreshold=41&highThreshold=71 */
    @GetMapping("/metrics")
    public ResponseEntity<EvaluationMetricsDto> getMetrics(
            @RequestParam(defaultValue = "41") int medThreshold,
            @RequestParam(defaultValue = "71") int highThreshold) {
        return ResponseEntity.ok(evaluationService.getMetrics(medThreshold, highThreshold));
    }

    /** GET /api/evaluation/settings */
    @GetMapping("/settings")
    public ResponseEntity<EngineSettingsDto> getSettings() {
        return ResponseEntity.ok(evaluationService.getSettings());
    }

    /** PUT /api/evaluation/settings */
    @PutMapping("/settings")
    public ResponseEntity<EngineSettingsDto> updateSettings(@RequestBody EngineSettingsDto dto) {
        return ResponseEntity.ok(evaluationService.updateSettings(dto));
    }
}
