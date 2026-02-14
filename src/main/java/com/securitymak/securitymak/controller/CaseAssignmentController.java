package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.CreateCaseAssignmentRequest;
import com.securitymak.securitymak.dto.CaseAssignmentResponse;
import com.securitymak.securitymak.service.CaseAssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/assignments")
@RequiredArgsConstructor
public class CaseAssignmentController {

    private final CaseAssignmentService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CaseAssignmentResponse assign(
            @PathVariable Long caseId,
            @Valid @RequestBody CreateCaseAssignmentRequest request
    ) {
        return service.assign(caseId, request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CaseAssignmentResponse> list(
            @PathVariable Long caseId
    ) {
        return service.list(caseId);
    }
}
