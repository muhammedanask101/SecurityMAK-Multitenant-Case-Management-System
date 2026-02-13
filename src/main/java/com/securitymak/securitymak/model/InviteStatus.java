package com.securitymak.securitymak.model;

public enum InviteStatus {

    PENDING,        // Created, not yet used
    REGISTERED,     // User submitted registration
    APPROVED,       // Admin approved, user activated
    REJECTED,       // Admin rejected registration
    TERMINATED      // Admin revoked before completion
}