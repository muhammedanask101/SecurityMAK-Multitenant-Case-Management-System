package com.securitymak.securitymak.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invites")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email this invite is bound to
    @Column(nullable = false)
    private String email;

    // Secure random token (UUID string)
    @Column(nullable = false, unique = true, updatable = false)
    private String token;

    // Tenant this invite belongs to
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // Role to be assigned upon approval
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Clearance to be assigned upon approval
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensitivityLevel clearanceLevel;

    // Lifecycle status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    // Admin who created this invite
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // If terminated/revoked
    private Instant terminatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminated_by_id")
    private User terminatedBy;

    // When invite was first used for registration
    private Instant registeredAt;

    // When admin approved registration
    private Instant approvedAt;

    // Audit timestamps
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = InviteStatus.PENDING;
        }
    }
}
