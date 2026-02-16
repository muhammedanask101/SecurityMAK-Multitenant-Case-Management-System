package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.ChangePasswordRequest;
import com.securitymak.securitymak.dto.DisableAccountRequest;
import com.securitymak.securitymak.dto.UserProfileResponse;
import com.securitymak.securitymak.service.AuthService;
import com.securitymak.securitymak.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse profile() {
        return userService.getCurrentUserProfile();
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public void changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(request);
    }

@PostMapping("/me/disable")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Void> disableOwnAccount(
        @RequestBody DisableAccountRequest request
) {
    userService.disableOwnAccount(request.password());
    return ResponseEntity.noContent().build();
}
}
