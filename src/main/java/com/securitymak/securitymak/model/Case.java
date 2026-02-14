package com.securitymak.securitymak.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // tenant boundary
    @Column(nullable = false)
    private Long tenantId;

    // ownership
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    // core data
    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensitivityLevel sensitivityLevel;

    // Indian legal metadata

    @Enumerated(EnumType.STRING)
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    private CourtLevel courtLevel;

    @Enumerated(EnumType.STRING)
    private CaseStage stage;

    @Column(unique = false)
    private String caseNumber;

    private String courtName;

    private String state;

    private String district;

    private LocalDateTime filingDate;

    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    private MatterType matterType;

    private String clientName;

    private String opposingPartyName;

    private String judgeName;

    private String assignedAdvocate;

    
}
