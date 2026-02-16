package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.CaseEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCaseEventRequest(

        @NotNull
        CaseEventType eventType,

        @Size(max = 5000)
        String description,

        LocalDate eventDate,

        LocalDate nextDate
) {}
