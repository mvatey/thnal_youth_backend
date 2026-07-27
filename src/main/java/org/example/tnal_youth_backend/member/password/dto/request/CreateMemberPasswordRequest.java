package org.example.tnal_youth_backend.member.password.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMemberPasswordRequest(

        @JsonProperty("new_password")
        @NotBlank(message = "New password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must be between 8 and 100 characters"
        )
        String newPassword,

        @JsonProperty("confirm_password")
        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}