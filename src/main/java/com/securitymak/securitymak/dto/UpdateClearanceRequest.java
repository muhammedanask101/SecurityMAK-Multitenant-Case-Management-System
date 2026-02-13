package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.SensitivityLevel;
import jakarta.validation.constraints.NotNull;

public record UpdateClearanceRequest(

        @NotNull(message = "Clearance level is required")
        SensitivityLevel clearanceLevel

) {}