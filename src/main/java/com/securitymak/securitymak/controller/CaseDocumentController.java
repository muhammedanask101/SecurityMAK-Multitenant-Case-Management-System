package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.CaseDocumentGroupResponse;
import com.securitymak.securitymak.dto.CaseDocumentResponse;
import com.securitymak.securitymak.model.SensitivityLevel;
import com.securitymak.securitymak.service.CaseDocumentService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/documents")
@RequiredArgsConstructor
public class CaseDocumentController {

    private final CaseDocumentService service;

    /* =====================================================
       UPLOAD DOCUMENT (MULTIPART)
    ====================================================== */
   @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@PreAuthorize("isAuthenticated()")
public CaseDocumentResponse upload(
        @PathVariable Long caseId,
        @RequestParam("file") MultipartFile file,
        @RequestParam("sensitivityLevel") SensitivityLevel sensitivityLevel,
        @RequestParam(value = "documentGroupId", required = false) String documentGroupId
) {
    return service.upload(caseId, file, sensitivityLevel, documentGroupId);
}

    /* =====================================================
       LIST DOCUMENTS
    ====================================================== */
       @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CaseDocumentGroupResponse> list(
            @PathVariable Long caseId
    ) {
        return service.list(caseId);
    }

    /* =====================================================
       DOWNLOAD DOCUMENT
    ====================================================== */
    @GetMapping("/{documentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(
            @PathVariable Long caseId,
            @PathVariable Long documentId
    ) {
        return service.download(documentId);
    }

    /* =====================================================
       SOFT DELETE (ADMIN ONLY)
    ====================================================== */
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(
            @PathVariable Long caseId,
            @PathVariable Long documentId
    ) {
        service.delete(documentId);
    }

    @PatchMapping("/{documentGroupId}/sensitivity")
@PreAuthorize("hasRole('ADMIN')")
public void updateSensitivity(
        @PathVariable Long caseId,
        @PathVariable String documentGroupId,
        @RequestParam SensitivityLevel sensitivityLevel
) {
    service.updateGroupSensitivity(caseId, documentGroupId, sensitivityLevel);
}
}
