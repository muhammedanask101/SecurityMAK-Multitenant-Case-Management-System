package com.securitymak.securitymak.dto;

import java.time.LocalDateTime;

public record AuditLogView(
        Long id,
        String actorEmail,
        String action,
        String targetType,
        Long targetId,
        String oldValue,
        String newValue,
        LocalDateTime timestamp
) {}
