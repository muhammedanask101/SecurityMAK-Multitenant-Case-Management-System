package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.CreateCaseEventRequest;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import com.securitymak.securitymak.dto.CaseEventResponse;
import com.securitymak.securitymak.model.*;
import com.securitymak.securitymak.repository.CaseEventRepository;
import com.securitymak.securitymak.repository.CaseRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseEventService {

    private final CaseEventRepository caseEventRepository;
    private final CaseRepository caseRepository;
    private final CaseAccessService caseAccessService;
    private final AuditService auditService;

    public CaseEventResponse addEvent(Long caseId, CreateCaseEventRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        boolean isAdmin = SecurityUtils.isAdmin();

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow();

        caseAccessService.validateTenantAccess(caseEntity);
        caseAccessService.validateCaseAccess(caseEntity, currentUser);

        boolean isOwner = caseEntity.getOwner().getId()
                .equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Only owner or admin can add events");
        }

        CaseEvent event = CaseEvent.builder()
                .tenantId(tenantId)
                .caseEntity(caseEntity)
                .eventType(request.eventType())
                .description(request.description())
                .eventDate(request.eventDate())
                .nextDate(request.nextDate())
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        CaseEvent saved = caseEventRepository.save(event);

        auditService.log(
                currentUser.getEmail(),
                AuditAction.CASE_UPDATED,
                "CASE_EVENT",
                saved.getId(),
                null,
                saved.getEventType().name(),
                tenantId
        );

        return toResponse(saved);
    }

    public List<CaseEventResponse> getEvents(Long caseId) {

        User currentUser = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow();

        caseAccessService.validateTenantAccess(caseEntity);
        caseAccessService.validateCaseAccess(caseEntity, currentUser);

        return caseEventRepository
                .findByTenantIdAndCaseEntityIdAndActiveTrueOrderByEventDateAsc(
                        tenantId,
                        caseId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteEvent(Long eventId) {

    SecurityUtils.requireAdmin();

    Long tenantId = SecurityUtils.getCurrentTenantId();
    String adminEmail = SecurityUtils.getCurrentUserEmail();

    CaseEvent event = caseEventRepository.findById(eventId)
            .orElseThrow();

    // Ensure tenant isolation
    if (!event.getTenantId().equals(tenantId)) {
        throw new UnauthorizedCaseAccessException();
    }

    // Soft delete
event.deactivate();
    caseEventRepository.save(event);

    auditService.log(
            adminEmail,
            AuditAction.CASE_UPDATED,
            "CASE_EVENT",
            eventId,
            "ACTIVE",
            "FALSE",
            tenantId
    );
}

    private CaseEventResponse toResponse(CaseEvent e) {
        return new CaseEventResponse(
                e.getId(),
                e.getEventType(),
                e.getDescription(),
                e.getEventDate(),
                e.getNextDate(),
                e.getCreatedBy().getEmail(),
                e.getCreatedAt()
        );
    }
}
