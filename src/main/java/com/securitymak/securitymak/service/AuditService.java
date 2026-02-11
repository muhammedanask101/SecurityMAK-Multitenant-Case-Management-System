package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.AuditLogView;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.AuditLog;
import com.securitymak.securitymak.repository.AuditLogRepository;
import com.securitymak.securitymak.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // 🔐 Unified logging method (ENUM-based)
    public void log(
            String actorEmail,
            AuditAction action,
            String targetType,
            Long targetId,
            String oldValue,
            String newValue,
            Long tenantId
    ) {

        AuditLog log = AuditLog.builder()
                .actorEmail(actorEmail)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .oldValue(oldValue)
                .newValue(newValue)
                .tenantId(tenantId)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    // 🔍 Paginated and filtered audit access
    public Page<AuditLogView> getAuditLogs(
            String actorEmail,
            AuditAction action,
            String targetType,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {

        Long tenantId = SecurityUtils.getCurrentTenantId();

        Page<AuditLog> page;

        if (actorEmail != null) {
            page = auditLogRepository
                    .findByTenantIdAndActorEmailContainingIgnoreCase(
                            tenantId, actorEmail, pageable);

        } else if (action != null) {
            page = auditLogRepository
                    .findByTenantIdAndAction(
                            tenantId, action, pageable);

        } else if (targetType != null) {
            page = auditLogRepository
                    .findByTenantIdAndTargetType(
                            tenantId, targetType, pageable);

        } else if (from != null && to != null) {
            page = auditLogRepository
                    .findByTenantIdAndTimestampBetween(
                            tenantId, from, to, pageable);

        } else {
            page = auditLogRepository
                    .findByTenantId(tenantId, pageable);
        }

        return page.map(this::toView);
    }

    private AuditLogView toView(AuditLog log) {
        return new AuditLogView(
                log.getId(),
                log.getActorEmail(),
                log.getAction().name(),
                log.getTargetType(),
                log.getTargetId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getTimestamp()
        );
    }
}