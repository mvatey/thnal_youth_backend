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

    /**
     * Returns the current principal's role name (e.g. {@code "ADMIN"},
     * {@code "BRANCH_LEADER"}) or {@code null} when it cannot be resolved.
     *
     * <p>Unlike {@link #getCurrentUserId()} this is intentionally NON-throwing: a
     * caller that only needs to know "is this a BRANCH_LEADER?" for data scoping
     * should treat an unknown role the same as a non-scoped one and fall back to
     * whatever object-level check it applies. Endpoints are still protected by
     * {@code @PreAuthorize} at the controller, so this is a data-scoping signal,
     * not an authentication gate.
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        User user = null;
        if (principal instanceof CustomUserDetails userDetails) {
            user = userDetails.getUser();
        } else if (principal instanceof User u) {
            user = u;
        }

        if (user != null && user.getRole() != null) {
            return user.getRole().name();
        }
        return null;
    }
}
