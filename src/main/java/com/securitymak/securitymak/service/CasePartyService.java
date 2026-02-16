package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.CreateCasePartyRequest;
import com.securitymak.securitymak.dto.UpdateCasePartyRequest;
import com.securitymak.securitymak.exception.ResourceNotFoundException;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import com.securitymak.securitymak.dto.CasePartyResponse;
import com.securitymak.securitymak.model.*;
import com.securitymak.securitymak.repository.CasePartyRepository;
import com.securitymak.securitymak.repository.CaseRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CasePartyService {

    private final CasePartyRepository casePartyRepository;
    private final CaseRepository caseRepository;
    private final CaseAccessService caseAccessService;
    private final AuditService auditService;

    public CasePartyResponse addParty(Long caseId, CreateCasePartyRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        boolean isAdmin = SecurityUtils.isAdmin();

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow();

        caseAccessService.validateTenantAccess(caseEntity);
        caseAccessService.validateCaseAccess(caseEntity, currentUser);
      caseAccessService.validateEditAccess(caseEntity, currentUser, isAdmin);

   


        CaseParty party = CaseParty.builder()
                .tenantId(tenantId)
                .caseEntity(caseEntity)
                .name(request.name())
                .role(request.role())
                .advocateName(request.advocateName())
                .contactInfo(request.contactInfo())
                .address(request.address())
                .notes(request.notes())
                .build();

        CaseParty saved = casePartyRepository.save(party);

        auditService.log(
                currentUser.getEmail(),
                AuditAction.CASE_UPDATED,
                "CASE_PARTY",
                saved.getId(),
                null,
                saved.getRole().name(),
                tenantId
        );

        return toResponse(saved);
    }

    public List<CasePartyResponse> getParties(Long caseId) {

        User currentUser = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow();

        caseAccessService.validateTenantAccess(caseEntity);
        caseAccessService.validateCaseAccess(caseEntity, currentUser);

        return casePartyRepository
                .findByTenantIdAndCaseEntityId(tenantId, caseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteParty(Long partyId) {

        SecurityUtils.requireAdmin();
        User currentUser = SecurityUtils.getCurrentUser();
boolean isAdmin = true;

        CaseParty party = casePartyRepository.findById(partyId)
                .orElseThrow();

                Case caseEntity = party.getCaseEntity();
caseAccessService.validateEditAccess(caseEntity, currentUser, isAdmin);
        casePartyRepository.delete(party);
    }

    private CasePartyResponse toResponse(CaseParty p) {
        return new CasePartyResponse(
                p.getId(),
                p.getName(),
                p.getRole(),
                p.getAdvocateName(),
                p.getContactInfo(),
                p.getAddress(),
                p.getNotes()
        );
    }

    public CasePartyResponse updateParty(
        Long caseId,
        Long partyId,
        UpdateCasePartyRequest request
) {

    User currentUser = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();
    boolean isAdmin = SecurityUtils.isAdmin();

    Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow();

    caseAccessService.validateTenantAccess(caseEntity);
    caseAccessService.validateCaseAccess(caseEntity, currentUser);
    caseAccessService.validateEditAccess(caseEntity, currentUser, isAdmin);

    CaseParty party = casePartyRepository.findById(partyId)
            .orElseThrow(() -> new ResourceNotFoundException("Party not found"));

    if (!party.getTenantId().equals(tenantId)) {
        throw new UnauthorizedCaseAccessException();
    }

   party.update(
        request.name(),
        request.role(),
        request.advocateName(),
        request.contactInfo(),
        request.address(),
        request.notes()
);

    casePartyRepository.save(party);

    auditService.log(
            currentUser.getEmail(),
            AuditAction.CASE_UPDATED,
            "CASE_PARTY_UPDATED",
            partyId,
            null,
            party.getRole().name(),
            tenantId
    );

    return toResponse(party);
}


}
