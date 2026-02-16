package com.securitymak.securitymak.dto;

import java.util.List;

public record CaseDocumentGroupResponse(
        String documentGroupId,
        List<CaseDocumentResponse> versions
) {}