package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.AuditLogView;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.AuditLog;
import com.securitymak.securitymak.repository.AuditLogRepository;
import com.securitymak.securitymak.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;

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

    Specification<AuditLog> spec = (root, query, cb) ->
            cb.equal(root.get("tenantId"), tenantId);

    if (actorEmail != null && !actorEmail.isBlank()) {
        spec = spec.and((root, query, cb) ->
                cb.like(
                        cb.lower(root.get("actorEmail")),
                        "%" + actorEmail.toLowerCase() + "%"
                )
        );
    }

    if (action != null) {
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("action"), action)
        );
    }

    if (targetType != null && !targetType.isBlank()) {
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("targetType"), targetType)
        );
    }

    if (from != null && to != null) {
        spec = spec.and((root, query, cb) ->
                cb.between(root.get("timestamp"), from, to)
        );
    }

    // Enforce newest-first ordering
    Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "timestamp")
    );

    Page<AuditLog> page =
            auditLogRepository.findAll(spec, sortedPageable);

    return page.map(this::toView);
}
}