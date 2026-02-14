package com.securitymak.securitymak.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseEventType eventType;

    @Column(length = 5000)
    private String description;

    private LocalDateTime eventDate;

    private LocalDateTime nextDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
private boolean active = true;

public void deactivate() {
    this.active = false;
}
}
