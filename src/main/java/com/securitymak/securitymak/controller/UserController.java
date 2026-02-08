package com.securitymak.securitymak.controller;

import com.securitymak.securitymak.dto.UserProfileResponse;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/user/profile")
    @PreAuthorize("hasRole('USER')")
    public UserProfileResponse profile(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
