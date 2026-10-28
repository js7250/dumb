package com.securebank.repository;

import com.securebank.entity.EngineSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineSettingsRepository extends JpaRepository<EngineSettings, Long> {
    // Uses findById(1L) — singleton row
}
