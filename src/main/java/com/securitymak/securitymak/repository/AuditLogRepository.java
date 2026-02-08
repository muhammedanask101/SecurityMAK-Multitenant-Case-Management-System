package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTenantId(
            Long tenantId,
            Pageable pageable
    );

    Page<AuditLog> findByTenantIdAndActorEmailContainingIgnoreCase(
            Long tenantId,
            String actorEmail,
            Pageable pageable
    );

    Page<AuditLog> findByTenantIdAndAction(
            Long tenantId,
            String action,
            Pageable pageable
    );

    Page<AuditLog> findByTenantIdAndTargetType(
            Long tenantId,
            String targetType,
            Pageable pageable
    );

    Page<AuditLog> findByTenantIdAndTimestampBetween(
            Long tenantId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}