package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetActivationPasswordRequest(

        @NotBlank(
                message = "Phone number or email is required"
        )
        String phoneOrEmail,

        @NotBlank(
                message = "OTP is required"
        )
        String otp,

        @NotBlank(
                message = "New password is required"
        )
        @Size(
                min = 6,
                message = "Password must contain at least 6 characters"
        )
        String newPassword
) {
}
