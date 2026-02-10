package com.securitymak.securitymak.service;

import com.securitymak.securitymak.dto.UserProfileResponse;
import com.securitymak.securitymak.model.User;
import com.securitymak.securitymak.repository.UserRepository;
import com.securitymak.securitymak.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getCurrentUserProfile() {

    User user = SecurityUtils.getCurrentUser();

    return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getRole().getName()
    );
}
}

