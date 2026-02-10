package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.CaseResponse;
import com.securitymak.securitymak.dto.CreateCaseRequest;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final AuditService auditService;

    public CaseResponse createCase(CreateCaseRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Case c = Case.builder()
                .title(request.title())
                .description(request.description())
                .status(CaseStatus.OPEN)
                .sensitivityLevel(SensitivityLevel.LOW)
                .owner(currentUser)
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build();

        caseRepository.save(c);

        auditService.log(
                currentUser.getEmail(),
                "CREATE_CASE",
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
                    "CROSS_TENANT_CASE_ACCESS_ATTEMPT",
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
                    "INVALID_CASE_STATUS_TRANSITION",
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
                "UPDATE_CASE_STATUS",
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
                .map(this::toResponse)
                .toList();
    }

    public List<CaseResponse> getAllCasesForTenant() {

        Long tenantId = SecurityUtils.getCurrentTenantId();

        return caseRepository
                .findAllByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<CaseResponse> getMyCases(Pageable pageable) {

        User user = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        return caseRepository
                .findByTenantIdAndOwnerId(tenantId, user.getId(), pageable)
                .map(this::toResponse);
    }

    public Page<CaseResponse> getTenantCases(Pageable pageable) {

        SecurityUtils.requireAdmin();

        Long tenantId = SecurityUtils.getCurrentTenantId();

        return caseRepository
                .findAllByTenantId(tenantId, pageable)
                .map(this::toResponse);
    }

    private CaseResponse toResponse(Case c) {
        return new CaseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getStatus(),
                c.getOwner().getEmail(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
