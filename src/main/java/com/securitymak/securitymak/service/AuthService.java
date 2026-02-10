package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.LoginRequest;
import com.securitymak.securitymak.dto.LoginResponse;
import com.securitymak.securitymak.dto.RegisterRequest;
import com.securitymak.securitymak.dto.UserProfileResponse;
import com.securitymak.securitymak.exception.*;
import com.securitymak.securitymak.model.Role;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse register(RegisterRequest request) {

         Tenant tenant = tenantRepository.findByCode("DEFAULT")
            .orElseThrow(() -> new RuntimeException("Default tenant missing"));

        if (userRepository.existsByEmailAndTenant_Id(request.getEmail(), tenant.getId())) {
                throw new BadRequestException("Email already registered");
        }


        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() ->
                        new RuntimeException("Default role USER not found"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .tenant(tenant)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
    }
    
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token);
        }

}


