package com.securitymak.securitymak.dto;

import java.time.LocalDateTime;

import com.securitymak.securitymak.model.CaseStatus;
import com.securitymak.securitymak.model.SensitivityLevel;

public record CaseResponse(
        Long id,
        String title,
        String description,
        CaseStatus status,
        SensitivityLevel sensitivityLevel,
        String ownerEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
