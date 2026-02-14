package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.SensitivityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCaseDocumentRequest(

        @NotBlank String fileName,
        @NotBlank String fileType,
        @NotBlank String storagePath,
        @NotNull SensitivityLevel sensitivityLevel

) {}
