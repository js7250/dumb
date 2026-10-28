package com.securebank.controller;

import com.securebank.dto.SimulatorRequest;
import com.securebank.entity.Transaction;
import com.securebank.service.AuditService;
import com.securebank.service.SimulatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

    private final SimulatorService simulatorService;
    private final AuditService     auditService;

    public SimulatorController(SimulatorService simulatorService, AuditService auditService) {
        this.simulatorService = simulatorService;
        this.auditService     = auditService;
    }

    /**
     * POST /api/simulator/generate
     * Generates a single synthetic transaction (used by the streaming UI).
     * Body: { "profileType": "NORMAL" | "SUSPICIOUS" | "FRAUD" }
     */
    @PostMapping("/generate")
    public ResponseEntity<Transaction> generateOne(@RequestBody Map<String, String> body,
                                                    @AuthenticationPrincipal UserDetails principal) {
        String profile = body.getOrDefault("profileType", "NORMAL");
        Transaction tx = simulatorService.generateSingle(profile);
        return ResponseEntity.ok(tx);
    }

    /**
     * POST /api/simulator/batch
     * Generates a full batch of transactions synchronously (for evaluation seeding).
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Transaction>> generateBatch(@Valid @RequestBody SimulatorRequest req,
                                                            @AuthenticationPrincipal UserDetails principal) {
        auditService.log(principal.getUsername(), "START_SIMULATION",
            String.format("Batch simulation: %d txs. Mix: %.0f%% N / %.0f%% S / %.0f%% F.",
                req.totalCount(), req.normalPct(), req.suspiciousPct(), req.fraudPct()));

        List<Transaction> results = simulatorService.generateBatch(
            req.totalCount(), req.normalPct(), req.suspiciousPct(), req.fraudPct()
        );
        return ResponseEntity.ok(results);
    }

    /** DELETE /api/simulator/purge — purge all simulated transactions */
    @DeleteMapping("/purge")
    public ResponseEntity<Map<String, String>> purge(@AuthenticationPrincipal UserDetails principal) {
        simulatorService.purgeSimulatedData(principal.getUsername());
        return ResponseEntity.ok(Map.of("message", "All simulated transactions purged successfully."));
    }
}
