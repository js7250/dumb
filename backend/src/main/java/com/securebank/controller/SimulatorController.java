package com.securebank.controller;

import com.securebank.dto.TransactionDto;
import com.securebank.service.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorService simulatorService;

    /** POST /api/simulator/generate — generate one synthetic transaction */
    @PostMapping("/generate")
    public ResponseEntity<TransactionDto> generateOne(@RequestBody Map<String, String> body) {
        String profileType = body.getOrDefault("profileType", "NORMAL");
        return ResponseEntity.ok(simulatorService.generateOne(profileType));
    }

    /** DELETE /api/simulator/purge — remove all simulated transactions */
    @DeleteMapping("/purge")
    public ResponseEntity<Void> purge() {
        simulatorService.purgeSimulated();
        return ResponseEntity.noContent().build();
    }
}
