package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.SensitivityLevel;

public record UserAdminView(
        Long id,
        String email,
        String role,
        SensitivityLevel clearanceLevel,
        String organizationName,
        boolean enabled
) {}
