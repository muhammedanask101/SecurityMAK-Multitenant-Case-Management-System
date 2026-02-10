package com.securitymak.securitymak.model;

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
    @Column(nullable = false)
    private String action;

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
