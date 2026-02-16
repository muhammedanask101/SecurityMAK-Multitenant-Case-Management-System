package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.CaseDocumentGroupResponse;
import com.securitymak.securitymak.dto.CaseDocumentResponse;
import com.securitymak.securitymak.exception.ForbiddenOperationException;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaseDocumentService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

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

    boolean isAdmin = user.getRole().getName().equals("ADMIN");
boolean isOwner = caseEntity.getOwner().getId().equals(user.getId());

if (!isAdmin && !isOwner) {

    auditService.log(
            user.getEmail(),
            AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
            "CASE_DOCUMENT_UPLOAD_DENIED",
            caseId,
            null,
            "Non-owner attempted document upload",
            tenantId
    );

    throw new ForbiddenOperationException(
            "Only case owner or admin can create documents"
    );
}
    

   accessService.validateEditAccess(caseEntity, user, isAdmin);

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

    repository
    .findByTenantIdAndCaseEntityIdAndFileHashAndActiveTrue(
        tenantId,
        caseId,
        hash
    )
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


    

 @Transactional(readOnly = true)
public List<CaseDocumentGroupResponse> list(Long caseId) {

    User user = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow();

    accessService.validateTenantAccess(caseEntity);
    accessService.validateCaseAccess(caseEntity, user);

    List<CaseDocument> documents =
            repository.findByTenantIdAndCaseEntityIdAndActiveTrue(tenantId, caseId);

    // ABAC filtering
    if (!user.getRole().getName().equals("ADMIN")) {
        documents = documents.stream()
                .filter(doc ->
                        user.getClearanceLevel()
                                .canAccess(doc.getSensitivityLevel()))
                .toList();
    }

    // Group by documentGroupId
    return documents.stream()
            .collect(Collectors.groupingBy(CaseDocument::getDocumentGroupId))
            .entrySet()
            .stream()
            .map(entry -> new CaseDocumentGroupResponse(
                    entry.getKey(),
                    entry.getValue().stream()
                            .sorted((a, b) -> b.getVersion() - a.getVersion())
                            .map(this::toResponse)
                            .toList()
            ))
            .toList();
}
    /* =====================================================
       DOWNLOAD DOCUMENT
    ====================================================== */
@Transactional(readOnly = true)
public ResponseEntity<Resource> download(Long documentId) {

    User user = SecurityUtils.getCurrentUser();
    Long tenantId = SecurityUtils.getCurrentTenantId();

    // Fetch tenant-safe & active document
    CaseDocument doc = repository
            .findByIdAndTenantIdAndActiveTrue(documentId, tenantId)
            .orElseThrow(UnauthorizedCaseAccessException::new);

    // 🔒 ABAC Clearance Check
    if (!user.getClearanceLevel()
            .canAccess(doc.getSensitivityLevel())) {
        throw new UnauthorizedCaseAccessException();
    }

    // 🔐 Validate case access WITHOUT touching lazy relationship
    accessService.validateCaseAccess(doc.getCaseEntity(), user);

    Resource resource =
            storageService.loadAsResource(doc.getStoragePath());

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

    User currentUser = SecurityUtils.getCurrentUser();

    CaseDocument doc = repository.findById(documentId)
            .orElseThrow();

    Case caseEntity = doc.getCaseEntity();

    accessService.validateTenantAccess(caseEntity);
    accessService.validateEditAccess(caseEntity, currentUser, true);

    doc.deactivate();
    repository.save(doc);

    auditService.log(
            currentUser.getEmail(),
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

    @Transactional
public void updateGroupSensitivity(
        Long caseId,
        String documentGroupId,
        SensitivityLevel newLevel
) {
    SecurityUtils.requireAdmin();

    Long tenantId = SecurityUtils.getCurrentTenantId();
    User user = SecurityUtils.getCurrentUser();

    List<CaseDocument> documents =
            repository.findByTenantIdAndCaseEntityIdAndDocumentGroupIdAndActiveTrue(
                    tenantId,
                    caseId,
                    documentGroupId
            );


    if (documents.isEmpty()) {
        throw new RuntimeException("Document group not found");
    }

    for (CaseDocument doc : documents) {
        SensitivityLevel oldLevel = doc.getSensitivityLevel();
        doc.updateSensitivity(newLevel);

        auditService.log(
                user.getEmail(),
                AuditAction.CASE_UPDATED,
                "CASE_DOCUMENT_SENSITIVITY_UPDATED",
                doc.getId(),
                oldLevel.name(),
                newLevel.name(),
                tenantId
        );
    }

    repository.saveAll(documents);
}
}
