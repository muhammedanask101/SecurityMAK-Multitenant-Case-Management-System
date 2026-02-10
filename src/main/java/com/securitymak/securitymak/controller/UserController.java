package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.UserProfileResponse;
import com.securitymak.securitymak.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/profile")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse profile() {
        return userService.getCurrentUserProfile();
    }
}
