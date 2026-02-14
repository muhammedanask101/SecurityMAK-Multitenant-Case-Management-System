package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.InviteStatus;
import com.securitymak.securitymak.model.SensitivityLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class InviteView {

    private Long id;
    private String email;
    private String token;
    private String role;
    private SensitivityLevel clearanceLevel;
    private InviteStatus status;

    private String createdBy;
    private Instant createdAt;
    private Instant registeredAt;
    private Instant approvedAt;
    private Instant terminatedAt;
}
