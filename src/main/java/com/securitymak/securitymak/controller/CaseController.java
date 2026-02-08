package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.CaseResponse;
import com.securitymak.securitymak.dto.CreateCaseRequest;
import com.securitymak.securitymak.dto.UpdateCaseStatusRequest;
import com.securitymak.securitymak.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    // USER + ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CaseResponse createCase(@RequestBody CreateCaseRequest request) {
        return caseService.createCase(request);
    }

    // USER: own cases only
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
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
}
