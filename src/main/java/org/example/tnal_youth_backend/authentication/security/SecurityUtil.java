package org.example.tnal_youth_backend.authentication.security;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static User getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        return user;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static String getCurrentUserRole() {
        return getCurrentUser().getRole().name();
    }

    public static boolean hasRole(String role) {
        return getCurrentUser()
                .getRole()
                .name()
                .equals(role);
    }
}