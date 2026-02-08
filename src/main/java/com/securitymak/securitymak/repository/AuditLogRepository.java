package com.securitymak.securitymak.repository;

import com.securitymak.securitymak.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByActorEmailContainingIgnoreCase(
            String actorEmail,
            Pageable pageable
    );

    Page<AuditLog> findByAction(
            String action,
            Pageable pageable
    );

    Page<AuditLog> findByTargetType(
            String targetType,
            Pageable pageable
    );

    Page<AuditLog> findByTimestampBetween(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}
