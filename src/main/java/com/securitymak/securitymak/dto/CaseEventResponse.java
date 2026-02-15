package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.CaseEventType;

import java.time.LocalDate;

import java.time.LocalDateTime;

public record CaseEventResponse(

        Long id,
        CaseEventType eventType,
        String description,
        LocalDate eventDate,
        LocalDate nextDate,
        String createdBy,
        LocalDateTime createdAt
) {}
