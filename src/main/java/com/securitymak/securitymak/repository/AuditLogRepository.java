package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface AuditLogRepository extends 
        JpaRepository<AuditLog, Long>,
        JpaSpecificationExecutor<AuditLog> {


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
        AuditAction action,
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