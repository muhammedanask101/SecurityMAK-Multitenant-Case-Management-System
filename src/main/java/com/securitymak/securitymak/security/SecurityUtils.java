package com.securitymak.securitymak.security;

import com.securitymak.securitymak.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Authentication getAuth() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }
        return auth;
    }

    public static String getCurrentUserEmail() {
        return getAuth().getName(); // safe
    }

    public static User getCurrentUser() {
        Object principal = getAuth().getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new RuntimeException(
                "Principal is not domain User: " + principal.getClass().getName()
        );
    }

    public static Long getCurrentTenantId() {
        return getCurrentUser().getTenant().getId();
    }

    public static void requireAdmin() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated access");
        }

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Admin privileges required");
        }
    }

    public static void requireSameTenant(User target) {
        if (!target.getTenant().getId().equals(getCurrentTenantId())) {
            throw new AccessDeniedException("Cross-tenant access denied");
        }
    }
}
