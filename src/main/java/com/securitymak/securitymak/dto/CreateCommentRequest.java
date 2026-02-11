package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.SensitivityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

        @NotBlank
        @Size(max = 5000)
        String content,

        @NotNull
        SensitivityLevel sensitivityLevel

) {}