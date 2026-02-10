package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.CaseResponse;
import com.securitymak.securitymak.dto.CreateCaseRequest;
import com.securitymak.securitymak.dto.UpdateCaseStatusRequest;
import com.securitymak.securitymak.service.CaseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    // USER + ADMIN
    @PostMapping(
        consumes = "application/json",
        produces = "application/json"
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CaseResponse createCase(@Valid @RequestBody CreateCaseRequest request) {
        return caseService.createCase(request);
    }

    // USER: own cases only
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<CaseResponse> myCases() {
        return caseService.getCasesForCurrentUser();
    }

    // ADMIN: all tenant cases
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<CaseResponse> allCases() {
        return caseService.getAllCasesForTenant();
    }

    @PutMapping("/{caseId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public CaseResponse updateStatus(
            @PathVariable Long caseId,
            @RequestBody UpdateCaseStatusRequest request
    ) {
        return caseService.updateCaseStatus(caseId, request.newStatus());
    }

    @GetMapping("/my/paged")
    @PreAuthorize("isAuthenticated()")
    public Page<CaseResponse> myCasesPaged(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return caseService.getMyCases(pageable);
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CaseResponse> allCasesPaged(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return caseService.getTenantCases(pageable);
    }
}
