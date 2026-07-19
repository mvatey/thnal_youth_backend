package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Phone or email is required")
    private String phoneOrEmail;

    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = "\\d{6}",
            message = "OTP must contain exactly 6 digits"
    )
    private String otp;

    @NotBlank(message = "New password is required")
    @Size(
            min = 8,
            max = 100,
            message = "Password must contain between 8 and 100 characters"
    )
    private String newPassword;
}