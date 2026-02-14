package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.AssignmentRole;

import java.time.LocalDateTime;

public record CaseAssignmentResponse(

        Long id,
        Long userId,
        String userEmail,
        AssignmentRole role,
        LocalDateTime assignedAt

) {}
