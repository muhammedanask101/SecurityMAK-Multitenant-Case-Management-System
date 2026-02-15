package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.CasePartyRole;

public record CasePartyResponse(

        Long id,
        String name,
        CasePartyRole role,
        String advocateName,
        String contactInfo,
        String address,
        String notes
) {}
