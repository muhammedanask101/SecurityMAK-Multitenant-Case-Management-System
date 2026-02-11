package com.securitymak.securitymak.model;
import com.securitymak.securitymak.model.AuditAction;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who performed the action
    @Column(nullable = false)
    private String actorEmail;

    // what happened
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    // target entity info
    private String targetType;   // USER
    private Long targetId;        // userId

    // details
    private String oldValue;
    private String newValue;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Long tenantId;

}
