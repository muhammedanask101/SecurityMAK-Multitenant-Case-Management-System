package com.securitymak.securitymak.service;

import com.securitymak.securitymak.exception.InvalidCaseTransitionException;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.Case;
import com.securitymak.securitymak.model.CaseStatus;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.CaseRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaseAccessService {

    private final AuditService auditService;
    private final CaseRepository caseRepository;

    public void validateTenantAccess(Case c) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        if (!c.getTenantId().equals(tenantId)) {

            auditService.log(
                SecurityUtils.getCurrentUserEmail(),
                AuditAction.CROSS_TENANT_ACCESS_ATTEMPT,
                "CASE",
                c.getId(),
                "tenant=" + c.getTenantId(),
                "tenant=" + tenantId,
                tenantId
        );

            throw new UnauthorizedCaseAccessException();
        }
    }

    public void validateCaseAccess(Case c, User user) {
        if (!user.getClearanceLevel()
                .canAccess(c.getSensitivityLevel())) {

        auditService.log(
            user.getEmail(),
            AuditAction.INSUFFICIENT_CLEARANCE_ATTEMPT,
            "CASE",
            c.getId(),
            user.getClearanceLevel().name(),
            c.getSensitivityLevel().name(),
            SecurityUtils.getCurrentTenantId()
        );


        throw new UnauthorizedCaseAccessException("Insufficient clearance");
        }
    }

 public void validateEditAccess(Case c, User user, boolean isAdmin) {

    boolean isOwner = c.getOwner().getId().equals(user.getId());
    CaseStatus status = c.getStatus();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    // 🔒 ARCHIVED = fully immutable
    if (status == CaseStatus.ARCHIVED) {

        auditService.log(
                user.getEmail(),
                AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
                "CASE",
                c.getId(),
                "ARCHIVED_EDIT_ATTEMPT",
                null,
                tenantId
        );

        throw new InvalidCaseTransitionException(
                "Archived cases are immutable"
        );
    }

    // 🔒 CLOSED = admin only
    if (status == CaseStatus.CLOSED && !isAdmin) {

        auditService.log(
                user.getEmail(),
                AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
                "CASE",
                c.getId(),
                "CLOSED_EDIT_ATTEMPT",
                null,
                tenantId
        );

        throw new UnauthorizedCaseAccessException(
                "Closed cases are editable by admin only"
        );
    }

    // 🔒 REVIEW = admin only
    if (status == CaseStatus.REVIEW && !isAdmin) {

        auditService.log(
                user.getEmail(),
                AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
                "CASE",
                c.getId(),
                "REVIEW_EDIT_ATTEMPT",
                null,
                tenantId
        );

        throw new UnauthorizedCaseAccessException(
                "Cases in review are editable by admin only"
        );
    }

    // 🔒 Ownership check for lower stages
    if (!isAdmin && !isOwner) {

        auditService.log(
                user.getEmail(),
                AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
                "CASE",
                c.getId(),
                "EDIT_ATTEMPT_NON_OWNER",
                null,
                tenantId
        );

        throw new UnauthorizedCaseAccessException(
                "Only owner or admin can edit"
        );
    }
}


}
