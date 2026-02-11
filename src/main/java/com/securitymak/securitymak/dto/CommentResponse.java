package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.SensitivityLevel;
import java.time.LocalDateTime;

public record CommentResponse(

        Long id,
        String authorEmail,
        String content,
        SensitivityLevel sensitivityLevel,
        LocalDateTime createdAt

) {}