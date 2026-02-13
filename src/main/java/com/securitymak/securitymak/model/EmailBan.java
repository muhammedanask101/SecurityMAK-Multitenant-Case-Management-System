package com.securitymak.securitymak.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "email_bans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tenant tenant; // null = global ban

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User bannedBy;

    @Column(nullable = false)
    private Instant bannedAt;

    private String reason;

    @PrePersist
    public void prePersist() {
        this.bannedAt = Instant.now();
    }
}
