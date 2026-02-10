package com.securitymak.securitymak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCaseRequest(
        @NotBlank String title,
        @NotBlank @Size(max = 5000) String description
) {}
