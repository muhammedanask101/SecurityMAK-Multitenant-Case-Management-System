package com.securitymak.securitymak.dto;

import java.time.LocalDateTime;

import com.securitymak.securitymak.model.CaseStatus;

public record CaseResponse(
        Long id,
        String title,
        String description,
        CaseStatus status,
        String ownerEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
