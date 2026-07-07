package org.example.tnal_youth_backend.security;

import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal p)) {
            throw new BusinessException("UNAUTHENTICATED", "No authenticated user in context");
        }
        return p;
    }

    public static Long getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    public static String getCurrentRole() {
        return getCurrentPrincipal().getRole();
    }
}