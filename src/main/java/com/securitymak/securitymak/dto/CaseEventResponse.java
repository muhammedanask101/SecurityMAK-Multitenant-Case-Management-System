package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.CaseEventType;

import java.time.LocalDateTime;

public record CaseEventResponse(

        Long id,
        CaseEventType eventType,
        String description,
        LocalDateTime eventDate,
        LocalDateTime nextDate,
        String createdBy,
        LocalDateTime createdAt
) {}
