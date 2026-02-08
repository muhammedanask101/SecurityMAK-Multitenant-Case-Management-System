package com.securitymak.securitymak.security;

import com.securitymak.securitymak.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
        // utility class
    }

    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        return auth.getName();
    }

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new RuntimeException(
                "Unexpected principal type: " + principal.getClass().getName()
        );
    }

    public static Long getCurrentTenantId() {
        return getCurrentUser().getTenant().getId();
    }
}
