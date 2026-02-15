package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.CaseDocumentResponse;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import com.securitymak.securitymak.model.*;
import com.securitymak.securitymak.repository.CaseDocumentRepository;
import com.securitymak.securitymak.repository.CaseRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import com.securitymak.securitymak.storage.FileStorageService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.UUID;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class CaseDocumentService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 10MB

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final CaseDocumentRepository repository;
    private final CaseRepository caseRepository;
    private final CaseAccessService accessService;
    private final AuditService auditService;
    private final FileStorageService storageService;

    /* =====================================================
       UPLOAD DOCUMENT (MULTIPART)
    ====================================================== */
public CaseDocumentResponse upload(
        Long caseId,
        MultipartFile file,
        SensitivityLevel sensitivityLevel,
        String documentGroupId // can be null
) {

    User user = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow();

    accessService.validateTenantAccess(caseEntity);
    accessService.validateCaseAccess(caseEntity, user);

    if (!user.getClearanceLevel().canAccess(sensitivityLevel)) {
        throw new UnauthorizedCaseAccessException("Insufficient clearance");
    }

    // File validation
    if (file.isEmpty()) {
        throw new RuntimeException("File cannot be empty");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
        throw new RuntimeException("File exceeds maximum allowed size");
    }

    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
        throw new RuntimeException("Unsupported file type");
    }

    // Determine group + version
    String groupId;
    int version;

    if (documentGroupId == null || documentGroupId.isBlank()) {
        groupId = UUID.randomUUID().toString();
        version = 1;
    } else {
        CaseDocument latest = repository
                .findTopByDocumentGroupIdOrderByVersionDesc(documentGroupId)
                .orElseThrow(() -> new RuntimeException("Invalid document group"));

        groupId = documentGroupId;
        version = latest.getVersion() + 1;
    }

    // Hash calculation
    String hash = calculateHash(file);

    repository.findByCaseEntityIdAndFileHashAndActiveTrue(caseId, hash)
        .ifPresent(existing -> {
            auditService.log(
                    user.getEmail(),
                    AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
                    "CASE_DOCUMENT_DUPLICATE",
                    existing.getId(),
                    null,
                    existing.getFileName(),
                    tenantId
            );

            throw new RuntimeException("Duplicate document detected in this case");
        });

    // Store physically
    String storedFileName = storageService.store(file);

    CaseDocument doc = CaseDocument.builder()
            .tenantId(tenantId)
            .caseEntity(caseEntity)
            .fileName(file.getOriginalFilename())
            .fileType(contentType)
            .fileSize(file.getSize())
            .fileHash(hash)
            .documentGroupId(groupId)
            .version(version)
            .storagePath(storedFileName)
            .sensitivityLevel(sensitivityLevel)
            .uploadedBy(user)
            .uploadedAt(LocalDateTime.now())
            .active(true)
            .build();

    CaseDocument saved = repository.save(doc);

    auditService.log(
            user.getEmail(),
            AuditAction.CASE_UPDATED,
            "CASE_DOCUMENT_VERSION_" + version,
            saved.getId(),
            null,
            saved.getFileName(),
            tenantId
    );

    return toResponse(saved);
}


    

    /* =====================================================
       LIST DOCUMENTS
    ====================================================== */
    public List<CaseDocumentResponse> list(Long caseId) {

    User user = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow();

    accessService.validateTenantAccess(caseEntity);
    accessService.validateCaseAccess(caseEntity, user);

    return repository
            .findByTenantIdAndCaseEntityIdAndActiveTrue(tenantId, caseId)
            .stream()
            .collect(Collectors.toMap(
                    CaseDocument::getDocumentGroupId,
                    doc -> doc,
                    (existing, replacement) ->
                            existing.getVersion() > replacement.getVersion()
                                    ? existing
                                    : replacement
            ))
            .values()
            .stream()
            .filter(doc -> user.getClearanceLevel()
                    .canAccess(doc.getSensitivityLevel()))
            .map(this::toResponse)
            .toList();
}
    /* =====================================================
       DOWNLOAD DOCUMENT
    ====================================================== */
    public ResponseEntity<Resource> download(Long documentId) {

        User user = SecurityUtils.getCurrentUser();
        Long tenantId = SecurityUtils.getCurrentTenantId();

        CaseDocument doc = repository.findById(documentId)
                .orElseThrow();

        if (!doc.getTenantId().equals(tenantId) || !doc.isActive()) {
            throw new UnauthorizedCaseAccessException();
        }

        accessService.validateCaseAccess(doc.getCaseEntity(), user);

        if (!user.getClearanceLevel()
                .canAccess(doc.getSensitivityLevel())) {
            throw new UnauthorizedCaseAccessException();
        }

        Resource resource = storageService.loadAsResource(doc.getStoragePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, doc.getFileType())
                .body(resource);
    }

    /* =====================================================
       SOFT DELETE (ADMIN ONLY)
    ====================================================== */
    public void delete(Long documentId) {

        SecurityUtils.requireAdmin();

        CaseDocument doc = repository.findById(documentId)
                .orElseThrow();

        doc.deactivate();
        repository.save(doc);

        auditService.log(
                SecurityUtils.getCurrentUserEmail(),
                AuditAction.CASE_UPDATED,
                "CASE_DOCUMENT_DELETED",
                doc.getId(),
                doc.getFileName(),
                null,
                doc.getTenantId()
        );
    }

    private String calculateHash(MultipartFile file) {
    try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hash generation failed", e);
        }
    }

    /* =====================================================
       DTO MAPPER
    ====================================================== */
    private CaseDocumentResponse toResponse(CaseDocument d) {
        return new CaseDocumentResponse(
                d.getId(),
                d.getFileName(),
                d.getFileType(),
                d.getFileSize(),
                d.getSensitivityLevel(),
                d.getUploadedBy().getEmail(),
                d.getUploadedAt(),
                
                d.getDocumentGroupId(),
                d.getVersion(),
                d.isActive(),
                d.getFileHash()
        );
    }
}
