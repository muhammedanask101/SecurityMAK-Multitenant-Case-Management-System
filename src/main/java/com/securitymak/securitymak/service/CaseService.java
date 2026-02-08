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

    private boolean isValidTransition(CaseStatus from, CaseStatus to) {
        return switch (from) {
            case OPEN -> to == CaseStatus.IN_REVIEW;
            case IN_REVIEW -> to == CaseStatus.CLOSED;
            case CLOSED -> false;
        };
    }

    public CaseResponse updateCaseStatus(Long caseId, CaseStatus newStatus) {

        User admin = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // tenant boundary
        if (!c.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Cross-tenant access denied");
        }

        CaseStatus oldStatus = c.getStatus();

        // no-op protection
        if (oldStatus == newStatus) {
            return toResponse(c);
        }

        // valid transitions only
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new RuntimeException(
                    "Invalid status transition: " + oldStatus + " → " + newStatus
            );
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
                .findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CaseResponse toResponse(Case c) {
        return new CaseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getStatus(),
                c.getOwner().getEmail(),
                c.getCreatedAt()
        );
    }
}
