package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.*;
import com.securitymak.securitymak.service.CasePartyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/parties")
@RequiredArgsConstructor
public class CasePartyController {

    private final CasePartyService casePartyService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public CasePartyResponse addParty(
            @PathVariable Long caseId,
            @Valid @RequestBody CreateCasePartyRequest request
    ) {
        return casePartyService.addParty(caseId, request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CasePartyResponse> getParties(
            @PathVariable Long caseId
    ) {
        return casePartyService.getParties(caseId);
    }

    @DeleteMapping("/{partyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteParty(
            @PathVariable Long partyId
    ) {
        casePartyService.deleteParty(partyId);
    }
}
