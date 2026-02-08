package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.AuditLogView;
import com.securitymak.securitymak.model.AuditLog;
import com.securitymak.securitymak.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            String actorEmail,
            String action,
            String targetType,
            Long targetId,
            String oldValue,
            String newValue
    ) {
        AuditLog log = AuditLog.builder()
                .actorEmail(actorEmail)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }

    // paginated and filtered access
    public Page<AuditLogView> getAuditLogs(
            String actorEmail,
            String action,
            String targetType,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {

        Page<AuditLog> page;

        if (actorEmail != null) {
            page = auditLogRepository
                    .findByActorEmailContainingIgnoreCase(actorEmail, pageable);
        } else if (action != null) {
            page = auditLogRepository
                    .findByAction(action, pageable);
        } else if (targetType != null) {
            page = auditLogRepository
                    .findByTargetType(targetType, pageable);
        } else if (from != null && to != null) {
            page = auditLogRepository
                    .findByTimestampBetween(from, to, pageable);
        } else {
            page = auditLogRepository.findAll(pageable);
        }

        return page.map(this::toView);
    }

    private AuditLogView toView(AuditLog log) {
        return new AuditLogView(
                log.getId(),
                log.getActorEmail(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getTimestamp()
        );
    }
}
