package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.CaseStatus;
import com.securitymak.securitymak.model.SensitivityLevel;

public record AdminCaseUpdateRequest(
        SensitivityLevel sensitivityLevel,
        CaseStatus status
) {}