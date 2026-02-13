package com.securitymak.securitymak.dto;

import com.securitymak.securitymak.model.SensitivityLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private UserView user;

    @Data
    @AllArgsConstructor
    public static class UserView {
        private Long id;
        private String email;
        private String role;
        private Long tenantId;
        private String organizationName;
        private SensitivityLevel clearanceLevel;
    }
}