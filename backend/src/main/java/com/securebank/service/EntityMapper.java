package com.securebank.service;

import com.securebank.dto.*;
import com.securebank.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public UserDto toUserDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        dto.setFullName(user.getFullName());
        dto.setAccountNumber(user.getAccountNumber());
        dto.setAccountType(user.getAccountType());
        dto.setBalance(user.getBalance());
        dto.setIsFrozen(user.getIsFrozen());
        dto.setCity(user.getCity());
        dto.setKnownDevices(user.getKnownDevices());
        dto.setAverageTxAmount(user.getAverageTxAmount());
        return dto;
    }

    public TransactionDto toTransactionDto(Transaction tx) {
        if (tx == null) return null;
        TransactionDto dto = new TransactionDto();
        dto.setId(tx.getId());
        dto.setUserId(tx.getUser().getId());
        dto.setAccountNumber(tx.getAccountNumber());
        dto.setType(tx.getType());
        dto.setAmount(tx.getAmount());
        dto.setPayee(tx.getPayee());
        dto.setCity(tx.getCity());
        dto.setDevice(tx.getDevice());
        dto.setTimestamp(tx.getTimestamp());
        dto.setRiskScore(tx.getRiskScore());
        dto.setRiskBand(tx.getRiskBand());
        dto.setReasons(tx.getReasons());
        dto.setStatus(tx.getStatus());
        dto.setIsSimulated(tx.getIsSimulated());
        dto.setTrueLabel(tx.getTrueLabel());
        return dto;
    }

    public AuditLogDto toAuditLogDto(AuditLog log) {
        if (log == null) return null;
        AuditLogDto dto = new AuditLogDto();
        dto.setId(log.getId());
        dto.setActor(log.getActor());
        dto.setAction(log.getAction());
        dto.setDetails(log.getDetails());
        dto.setRiskScore(log.getRiskScore());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }

    public EngineSettingsDto toEngineSettingsDto(EngineSettings s) {
        EngineSettingsDto dto = new EngineSettingsDto();
        dto.setWeightAmountDeviation(s.getWeightAmountDeviation());
        dto.setWeightAbsoluteHighAmount(s.getWeightAbsoluteHighAmount());
        dto.setWeightOddHours(s.getWeightOddHours());
        dto.setWeightUnusualCity(s.getWeightUnusualCity());
        dto.setWeightUnknownDevice(s.getWeightUnknownDevice());
        dto.setWeightVelocityBurst(s.getWeightVelocityBurst());
        dto.setThresholdMediumRiskMin(s.getThresholdMediumRiskMin());
        dto.setThresholdHighRiskMin(s.getThresholdHighRiskMin());
        return dto;
    }

    public void applyEngineSettingsDto(EngineSettingsDto dto, EngineSettings entity) {
        entity.setWeightAmountDeviation(dto.getWeightAmountDeviation());
        entity.setWeightAbsoluteHighAmount(dto.getWeightAbsoluteHighAmount());
        entity.setWeightOddHours(dto.getWeightOddHours());
        entity.setWeightUnusualCity(dto.getWeightUnusualCity());
        entity.setWeightUnknownDevice(dto.getWeightUnknownDevice());
        entity.setWeightVelocityBurst(dto.getWeightVelocityBurst());
        entity.setThresholdMediumRiskMin(dto.getThresholdMediumRiskMin());
        entity.setThresholdHighRiskMin(dto.getThresholdHighRiskMin());
    }
}
