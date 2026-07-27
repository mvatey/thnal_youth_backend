package org.example.tnal_youth_backend.account.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;

public record CreateUserRequest(

        @Size(
                max = 30,
                message = "Phone must not exceed 30 characters"
        )
        String phone,

        @Email(message = "Email format is invalid")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must contain between 8 and 100 characters"
        )
        String password,

        @NotNull(message = "Role is required")
        UserRole role,

        @NotNull(message = "Status is required")
        UserStatus status,

        @NotBlank(message = "Khmer full name is required")
        @Size(
                max = 255,
                message = "Khmer full name must not exceed 255 characters"
        )
        String fullNameKm,

        @Size(
                max = 255,
                message = "English full name must not exceed 255 characters"
        )
        String fullNameEn,

        @Size(
                max = 500,
                message = "Profile image must not exceed 500 characters"
        )
        String profileImage

) {
}