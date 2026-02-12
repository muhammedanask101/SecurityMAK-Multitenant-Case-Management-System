package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.CaseResponse;
import com.securitymak.securitymak.dto.CreateCaseRequest;
import com.securitymak.securitymak.dto.UpdateCaseRequest;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.Case;
import com.securitymak.securitymak.model.CaseStatus;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.CaseRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.securitymak.securitymak.model.SensitivityLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.securitymak.securitymak.exception.CaseNotFoundException;
import com.securitymak.securitymak.exception.InvalidCaseTransitionException;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final AuditService auditService;
    private final CaseAccessService caseAccessService;

    public CaseResponse createCase(CreateCaseRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        SensitivityLevel requestedLevel = request.sensitivityLevel();

        if (!currentUser.getClearanceLevel()
        .canAccess(requestedLevel)) {
            throw new UnauthorizedCaseAccessException(
                    "Cannot create case above your clearance level"
            );
        }

        Case c = Case.builder()
                .title(request.title())
                .description(request.description())
                .status(CaseStatus.OPEN)
                .sensitivityLevel(requestedLevel)
                .owner(currentUser)
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build();

        caseRepository.save(c);

        auditService.log(
                currentUser.getEmail(),
                AuditAction.CASE_CREATED,
                "CASE",
                c.getId(),
                null,
                CaseStatus.OPEN.name(),
                tenantId
        );

        return toResponse(c);
    }


    public CaseResponse updateCaseStatus(Long caseId, CaseStatus newStatus) {

        SecurityUtils.requireAdmin();

        User admin = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Case c = caseRepository.findById(caseId)
        .orElseThrow(CaseNotFoundException::new);

        if (!c.getTenantId().equals(tenantId)) {

            auditService.log(
                    SecurityUtils.getCurrentUserEmail(),
                    AuditAction.CROSS_TENANT_ACCESS_ATTEMPT,
                    "CASE",
                    caseId,
                    "tenant=" + c.getTenantId(),
                    "tenant=" + tenantId,
                    tenantId
            );

            throw new UnauthorizedCaseAccessException();
        }

        CaseStatus oldStatus = c.getStatus();

        // no-op protection
        if (oldStatus == newStatus) {
            return toResponse(c);
        }

        // valid transitions only
        if (!oldStatus.canTransitionTo(newStatus)) {

            auditService.log(
                    admin.getEmail(),
                    AuditAction.INVALID_STATUS_TRANSITION,
                    "CASE",
                    c.getId(),
                    oldStatus.name(),
                    newStatus.name(),
                    tenantId
            );

            throw new InvalidCaseTransitionException();
        }

        c.setStatus(newStatus);
        caseRepository.save(c);

        // audit log
        auditService.log(
                admin.getEmail(),
                AuditAction.CASE_STATUS_CHANGED,
                "CASE",
                c.getId(),
                oldStatus.name(),
                newStatus.name(),
                tenantId
        );

        return toResponse(c);
    }

    public List<CaseResponse> getCasesForCurrentUser() {

    User currentUser = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    return caseRepository
            .findByTenantIdAndOwnerId(tenantId, currentUser.getId())
            .stream()
            .filter(c ->
                currentUser.getClearanceLevel()
                        .canAccess(c.getSensitivityLevel())
            )
            .map(this::toResponse)
            .toList();
}

    public List<CaseResponse> getAllCasesForTenant() {

        User currentUser = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        return caseRepository
                .findAllByTenantId(tenantId)
                .stream()
                .filter(c ->
                    currentUser.getClearanceLevel()
                            .canAccess(c.getSensitivityLevel())
                )
                .map(this::toResponse)
                .toList();
    }

 public Page<CaseResponse> getMyCases(Pageable pageable) {

    User user = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    return caseRepository
            .findByTenantIdAndOwnerId(tenantId, user.getId(), pageable)
            .map(c -> {
                if (!user.getClearanceLevel()
                        .canAccess(c.getSensitivityLevel())) {
                    throw new UnauthorizedCaseAccessException("Insufficient clearance");
                }
                return toResponse(c);
            });
}

public Page<CaseResponse> getTenantCases(Pageable pageable) {

    SecurityUtils.requireAdmin();

    User user = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    return caseRepository
            .findByTenantIdAndSensitivityLevelLessThanEqual(
                    tenantId,
                    user.getClearanceLevel(),
                    pageable
            )
            .map(this::toResponse);
}
                

    private CaseResponse toResponse(Case c) {
        return new CaseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getStatus(),
                c.getSensitivityLevel(),  
                c.getOwner().getEmail(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    @Transactional
public CaseResponse updateCase(Long caseId, UpdateCaseRequest request) {

    User currentUser = SecurityUtils.getCurrentUser();
    Long currentTenantId = SecurityUtils.getCurrentTenantId();
    boolean isAdmin = SecurityUtils.isAdmin();

    Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow(CaseNotFoundException::new);

    caseAccessService.validateTenantAccess(caseEntity);

    caseAccessService.validateCaseAccess(caseEntity, currentUser);

    caseAccessService.validateEditAccess(caseEntity, currentUser, isAdmin);

    // 5️⃣ Capture old values for audit
    String oldTitle = caseEntity.getTitle();
    String oldDescription = caseEntity.getDescription();

    // 6️⃣ Apply updates
    caseEntity.setTitle(request.title());
    caseEntity.setDescription(request.description());

    caseRepository.save(caseEntity);

    // 7️⃣ Audit logging (simple version using existing log method)
    if (!oldTitle.equals(request.title())) {
        auditService.log(
                currentUser.getEmail(),
                AuditAction.CASE_UPDATED,
                "CASE",
                caseId,
                oldTitle,
                request.title(),
                currentTenantId
        );
    }

    if (!oldDescription.equals(request.description())) {
        auditService.log(
                currentUser.getEmail(),
                AuditAction.CASE_UPDATED,
                "CASE",
                caseId,
                "[REDACTED]",
                "[REDACTED]",
                currentTenantId
        );
    }

    return toResponse(caseEntity);
}

public CaseResponse getCaseById(Long caseId) {

    User user = SecurityUtils.getCurrentUser();

    Case c = caseRepository.findById(caseId)
            .orElseThrow(CaseNotFoundException::new);

    caseAccessService.validateTenantAccess(c);
    caseAccessService.validateCaseAccess(c, user);

    auditService.log(
        user.getEmail(),
        AuditAction.CASE_VIEWED,
        "CASE",
        caseId,
        null,
        null,
        SecurityUtils.getCurrentTenantId()
    );

    return toResponse(c);
}

public Page<CaseResponse> listCases(Pageable pageable) {

    SecurityUtils.requireAdmin();

    Long tenantId = SecurityUtils.getCurrentTenantId();

    return caseRepository
            .findAllByTenantId(tenantId, pageable)
            .map(this::toResponse);
}

@Transactional
public CaseResponse updateCaseSensitivity(Long caseId, SensitivityLevel newLevel) {

    SecurityUtils.requireAdmin();

    User admin = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    Case c = caseRepository.findById(caseId)
            .orElseThrow(CaseNotFoundException::new);

    caseAccessService.validateTenantAccess(c);

    SensitivityLevel oldLevel = c.getSensitivityLevel();

    c.setSensitivityLevel(newLevel);
    caseRepository.save(c);

    auditService.log(
            admin.getEmail(),
            AuditAction.CASE_SENSITIVITY_CHANGED,
            "CASE",
            caseId,
            oldLevel.name(),
            newLevel.name(),
            tenantId
    );

    return toResponse(c);
}
}
