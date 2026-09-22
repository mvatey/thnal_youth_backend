package org.example.tnal_youth_backend.systemsettings.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.systemsettings.dto.request.RevealDefaultMemberPasswordRequest;
import org.example.tnal_youth_backend.systemsettings.dto.request.UpdateDefaultMemberPasswordRequest;
import org.example.tnal_youth_backend.systemsettings.service.SystemSettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;

    @PostMapping("/default-member-password/reveal")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> revealDefaultMemberPassword(
            @Valid @RequestBody RevealDefaultMemberPasswordRequest request,
            Authentication authentication
    ) {
        String value = systemSettingsService.revealDefaultMemberPassword(
                extractCurrentUserId(authentication),
                request.currentPassword()
        );

        return Map.of("default_password", value);
    }

    @PatchMapping("/default-member-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> updateDefaultMemberPassword(
            @Valid @RequestBody UpdateDefaultMemberPasswordRequest request,
            Authentication authentication
    ) {
        String value = systemSettingsService.updateDefaultMemberPassword(
                extractCurrentUserId(authentication),
                request.currentPassword(),
                request.newDefaultPassword()
        );

        return Map.of("default_password", value);
    }

    private Long extractCurrentUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof org.example.tnal_youth_backend.authentication.security.CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user information is invalid"
        );
    }
}
