package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.AssignmentRole;
import jakarta.validation.constraints.NotNull;

public record CreateCaseAssignmentRequest(
        @NotNull String userEmail,
        @NotNull AssignmentRole role
) {}