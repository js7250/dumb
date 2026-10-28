package com.securebank.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class AuditLogDto {
    private Long id;
    private String actor;
    private String action;
    private String details;
    private Integer riskScore;
    private Instant timestamp;
}
