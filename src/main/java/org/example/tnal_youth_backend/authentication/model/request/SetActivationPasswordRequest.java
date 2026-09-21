package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.tnal_youth_backend.common.validation.PasswordPolicy;

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
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
        String newPassword
) {
}
