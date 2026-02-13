package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.ChangePasswordRequest;
import com.securitymak.securitymak.dto.LoginRequest;
import com.securitymak.securitymak.dto.LoginResponse;
import com.securitymak.securitymak.dto.RegisterRequest;
import com.securitymak.securitymak.dto.UserProfileResponse;
import com.securitymak.securitymak.exception.*;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.Role;
import com.securitymak.securitymak.model.SensitivityLevel;
import com.securitymak.securitymak.model.Tenant;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.RoleRepository;
import com.securitymak.securitymak.repository.UserRepository;
import com.securitymak.securitymak.repository.TenantRepository;
import com.securitymak.securitymak.security.JwtService;
import com.securitymak.securitymak.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public LoginResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already registered");
        }

        if (request.getOrganizationName() == null || request.getOrganizationName().isBlank()) {
                throw new BadRequestException("Organization name is required");
        }

        String orgName = request.getOrganizationName().trim();

        String code = orgName
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-");

        if (tenantRepository.existsByCode(code)) {
                throw new BadRequestException("Organization code already exists");
        }

        if (tenantRepository.existsByName(request.getOrganizationName())) {
        throw new BadRequestException("Organization name already taken");
        }


        Role adminRole = roleRepository.findByName("ADMIN")
        .orElseThrow(() ->
                new RuntimeException("Default role ADMIN not found"));

        Tenant tenant = Tenant.builder()
                .name(orgName)
                .code(code)
                .build();

        tenantRepository.save(tenant);

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(adminRole)
                .tenant(tenant)
                .clearanceLevel(SensitivityLevel.CRITICAL) 
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        auditService.log(
        savedUser.getEmail(),
        AuditAction.USER_REGISTERED,
        "USER_REGISTERED",
        savedUser.getId(),
        null,
        null,
        savedUser.getTenant().getId()
);

        return new LoginResponse(
        token,
        new LoginResponse.UserView(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole().getName(),
            savedUser.getTenant().getId(),
            user.getTenant().getName(),
            savedUser.getClearanceLevel()  
        )
    );

    }
    
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        auditService.log(
        user.getEmail(),
        AuditAction.USER_LOGIN,
        "USER_LOGIN",
        user.getId(),
        null,
        null,
        user.getTenant().getId()
);

        return new LoginResponse(
                token,
                new LoginResponse.UserView(
                user.getId(),
                user.getEmail(),
                user.getRole().getName(),
                user.getTenant().getId(),
                user.getTenant().getName(),
                user.getClearanceLevel()  
                )
        );
        }

        @Transactional
        public void changePassword(ChangePasswordRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(
                request.currentPassword(),
                currentUser.getPassword()
        )) {
                throw new UnauthorizedException("Current password is incorrect");
        }

        // Prevent same password reuse
        if (passwordEncoder.matches(
                request.newPassword(),
                currentUser.getPassword()
        )) {
                throw new BadRequestException("New password must be different");
        }

        currentUser.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        userRepository.save(currentUser);

        auditService.log(
                currentUser.getEmail(),
                AuditAction.PASSWORD_CHANGED,
                "USER",
                currentUser.getId(),
                null,
                null,
                currentUser.getTenant().getId()
        );
        }

}


