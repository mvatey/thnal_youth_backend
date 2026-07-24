package org.example.tnal_youth_backend.authentication.security;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {

            throw new RuntimeException("User is not authenticated");
        }

        return userDetails.getUser();
    }
}