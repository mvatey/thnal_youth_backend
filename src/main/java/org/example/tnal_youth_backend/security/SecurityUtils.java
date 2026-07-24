package org.example.tnal_youth_backend.security;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("UNAUTHENTICATED", "User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();
            if (user != null && user.getId() != null) {
                return user.getId();
            }
        } else if (principal instanceof User user) {
            if (user.getId() != null) {
                return user.getId();
            }
        }

        throw new BusinessException("UNAUTHENTICATED", "User is not authenticated");
    }
}
