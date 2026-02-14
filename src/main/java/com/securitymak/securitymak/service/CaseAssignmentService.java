package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.CreateCaseAssignmentRequest;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import com.securitymak.securitymak.dto.CaseAssignmentResponse;
import com.securitymak.securitymak.model.*;
import com.securitymak.securitymak.repository.CaseAssignmentRepository;
import com.securitymak.securitymak.repository.CaseRepository;
import com.securitymak.securitymak.repository.UserRepository;
import com.securitymak.securitymak.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseAssignmentService {

    private final CaseAssignmentRepository repository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final CaseAccessService accessService;
    private final AuditService auditService;

    public CaseAssignmentResponse assign(Long caseId, CreateCaseAssignmentRequest request) {

        SecurityUtils.requireAdmin();

        User admin = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow();

        accessService.validateTenantAccess(caseEntity);

        User assignedUser = userRepository.findById(request.userId())
                .orElseThrow();

        if (!assignedUser.getTenant().getId().equals(tenantId)) {
            throw new UnauthorizedCaseAccessException("Cannot assign user from another tenant");
        }

        // Ensure single LEAD per case
        if (request.role() == AssignmentRole.LEAD) {

            boolean existsLead = repository.existsByTenantIdAndCaseEntityIdAndUserIdAndRole(
                    tenantId,
                    caseId,
                    assignedUser.getId(),
                    AssignmentRole.LEAD
            );

            if (existsLead) {
                throw new RuntimeException("Lead already assigned");
            }
        }

        CaseAssignment assignment = CaseAssignment.builder()
                .tenantId(tenantId)
                .caseEntity(caseEntity)
                .user(assignedUser)
                .role(request.role())
                .assignedAt(LocalDateTime.now())
                .build();

        CaseAssignment saved = repository.save(assignment);

        auditService.log(
                admin.getEmail(),
                AuditAction.CASE_UPDATED,
                "CASE_ASSIGNMENT",
                saved.getId(),
                null,
                saved.getRole().name(),
                tenantId
        );

        return toResponse(saved);
    }

    public List<CaseAssignmentResponse> list(Long caseId) {

        Long tenantId = SecurityUtils.getCurrentTenantId();

        return repository.findByTenantIdAndCaseEntityId(tenantId, caseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CaseAssignmentResponse toResponse(CaseAssignment a) {
        return new CaseAssignmentResponse(
                a.getId(),
                a.getUser().getId(),
                a.getUser().getEmail(),
                a.getRole(),
                a.getAssignedAt()
        );
    }
}

