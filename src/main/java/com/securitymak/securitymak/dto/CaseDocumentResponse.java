package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.SensitivityLevel;

import java.time.LocalDateTime;

public record CaseDocumentResponse(

        Long id,
        String fileName,
        String fileType,
        Long fileSize,
        SensitivityLevel sensitivityLevel,
        String uploadedBy,
        LocalDateTime uploadedAt,

        // Enterprise metadata
        String documentGroupId,
        Integer version,
        Boolean active,
        String fileHash

) {}
