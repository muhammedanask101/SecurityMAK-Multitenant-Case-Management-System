package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.ChangePasswordRequest;
import com.securitymak.securitymak.dto.UserProfileResponse;
import com.securitymak.securitymak.service.AuthService;
import com.securitymak.securitymak.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/api/users/profile")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse profile() {
        return userService.getCurrentUserProfile();
    }

    @PutMapping("/api/users/change-password")
    @PreAuthorize("isAuthenticated()")
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(request);
    }
}
