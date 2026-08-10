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
                min = 8,
                max = 72,
                message = "Password must contain between 8 and 72 characters"
        )
        String newPassword
) {
}
