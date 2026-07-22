package org.example.tnal_youth_backend.account.user.dto.response;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;

import java.time.OffsetDateTime;

public record UserResponse(

        Long id,
        String phone,
        String email,
        UserRole role,
        UserStatus status,
        String fullNameKm,
        String fullNameEn,
        String profileImage,
        OffsetDateTime lastLoginAt,
        Integer failedLoginCount,
        OffsetDateTime lockedUntil,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {
}