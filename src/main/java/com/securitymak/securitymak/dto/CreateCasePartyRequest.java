package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.CasePartyRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCasePartyRequest(

        @NotBlank
        String name,

        @NotNull
        CasePartyRole role,

        String advocateName,

        String contactInfo,

        String address,

        String notes
) {}
