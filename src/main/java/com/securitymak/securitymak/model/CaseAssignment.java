package com.securitymak.securitymak.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "case_assignments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentRole role;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    @Column(nullable = false)
private boolean active = true;

public void deactivate() {
    this.active = false;
}
}
