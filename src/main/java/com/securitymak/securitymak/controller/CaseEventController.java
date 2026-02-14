package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.*;
import com.securitymak.securitymak.service.CaseEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/events")
@RequiredArgsConstructor
public class CaseEventController {

    private final CaseEventService caseEventService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public CaseEventResponse addEvent(
            @PathVariable Long caseId,
            @Valid @RequestBody CreateCaseEventRequest request
    ) {
        return caseEventService.addEvent(caseId, request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CaseEventResponse> getEvents(
            @PathVariable Long caseId
    ) {
        return caseEventService.getEvents(caseId);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEvent(
            @PathVariable Long eventId
    ) {
        caseEventService.deleteEvent(eventId);
    }
}

