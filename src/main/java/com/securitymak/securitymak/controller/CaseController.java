package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.CaseResponse;
import com.securitymak.securitymak.dto.CreateCaseRequest;
import com.securitymak.securitymak.dto.UpdateCaseRequest;
import com.securitymak.securitymak.dto.UpdateCaseStatusRequest;
import com.securitymak.securitymak.model.SensitivityLevel;
import com.securitymak.securitymak.service.CaseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.securitymak.securitymak.dto.UpdateCaseRequest;
import com.securitymak.securitymak.model.SensitivityLevel;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
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



    @PutMapping("/{caseId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public CaseResponse updateStatus(
            @PathVariable Long caseId,
            @RequestBody UpdateCaseStatusRequest request
    ) {
        return caseService.updateCaseStatus(caseId, request.newStatus());
    }

    @GetMapping("/my")
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

    @GetMapping("/{caseId}")
    @PreAuthorize("isAuthenticated()")
    public CaseResponse getCaseById(@PathVariable Long caseId) {
        return caseService.getCaseById(caseId);
    }

    @PutMapping("/{caseId}")
    @PreAuthorize("isAuthenticated()")
    public CaseResponse updateCase(
            @PathVariable Long caseId,
            @Valid @RequestBody UpdateCaseRequest request
    ) {
        return caseService.updateCase(caseId, request);
    }

    @PatchMapping("/{caseId}/sensitivity")
    @PreAuthorize("hasRole('ADMIN')")
    public CaseResponse updateSensitivity(
            @PathVariable Long caseId,
            @RequestParam SensitivityLevel level
    ) {
        return caseService.updateCaseSensitivity(caseId, level);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CaseResponse> listCases(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return caseService.listCases(pageable);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Page<CaseResponse> myCases(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return caseService.getMyCases(pageable);
    }
}
