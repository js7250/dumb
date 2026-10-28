package com.securebank.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class TransactionDto {
    private String id;
    private Long userId;
    private String accountNumber;
    private String type;
    private Double amount;
    private String payee;
    private String city;
    private String device;
    private Instant timestamp;
    private Integer riskScore;
    private String riskBand;
    private List<String> reasons;
    private String status;
    private Boolean isSimulated;
    private String trueLabel;
}
