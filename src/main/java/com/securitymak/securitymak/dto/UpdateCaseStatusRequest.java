package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.CaseStatus;

public record UpdateCaseStatusRequest(
        CaseStatus newStatus
) {}
