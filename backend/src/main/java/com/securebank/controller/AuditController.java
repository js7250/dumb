package com.securebank.controller;

import com.securebank.dto.AuditLogDto;
import com.securebank.repository.AuditLogRepository;
import com.securebank.service.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final EntityMapper mapper;

    /** GET /api/audit — returns all audit logs, newest first */
    @GetMapping
    public ResponseEntity<List<AuditLogDto>> getAll() {
        return ResponseEntity.ok(
                auditLogRepository.findAllByOrderByTimestampDesc()
                        .stream().map(mapper::toAuditLogDto).toList());
    }
}
