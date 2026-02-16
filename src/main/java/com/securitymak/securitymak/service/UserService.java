package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.UserProfileResponse;
import com.securitymak.securitymak.model.AuditAction;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.UserRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public UserProfileResponse getCurrentUserProfile() {

        User user = SecurityUtils.getCurrentUser();

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().getName()
        );
    }

@Transactional
public void disableOwnAccount(String rawPassword) {

    User principalUser = SecurityUtils.getCurrentUser();

    User currentUser = userRepository.findById(principalUser.getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));

    if ("ADMIN".equals(currentUser.getRole().getName())) {
        throw new IllegalStateException("Administrators cannot disable their own account.");
    }

    if (!passwordEncoder.matches(rawPassword, currentUser.getPassword())) {
        throw new IllegalArgumentException("Invalid password.");
    }

    currentUser.setEnabled(false);

    auditService.log(
            currentUser.getEmail(),
            AuditAction.ACCOUNT_SELF_DISABLED,
            "USER",
            currentUser.getId(),
            "enabled=true",
            "enabled=false",
            currentUser.getTenant().getId()
    );
}
}

