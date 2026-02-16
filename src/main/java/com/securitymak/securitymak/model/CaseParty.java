package com.securitymak.securitymak.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "case_parties")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseParty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CasePartyRole role;

    private String advocateName;

    private String contactInfo;

    private String address;

@Builder.Default
@Column(nullable = false)
private boolean active = true;

@Column(columnDefinition = "TEXT")
private String notes;

public void deactivate() {
    this.active = false;
}

public void update(
        String name,
        CasePartyRole role,
        String advocateName,
        String contactInfo,
        String address,
        String notes
) {
    this.name = name;
    this.role = role;
    this.advocateName = advocateName;
    this.contactInfo = contactInfo;
    this.address = address;
    this.notes = notes;
}
}
