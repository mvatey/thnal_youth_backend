package org.example.tnal_youth_backend.account.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record UpdateUserRequest(

        @JsonProperty("member_id")
        @Positive(message = "Member ID must be positive")
        Long memberId,

        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^[0-9+() -]{6,30}$",
                message = "Phone format is invalid"
        )
        String phone,

        @Email(message = "Email format is invalid")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email,

        @Size(
                min = 8,
                max = 100,
                message = "Password must contain between 8 and 100 characters"
        )
        String password,

        @JsonProperty("role_id")
        @NotNull(message = "Role ID is required")
        @Positive(message = "Role ID must be positive")
        Short roleId,

        @JsonProperty("account_status_id")
        @NotNull(message = "Account status ID is required")
        @Positive(message = "Account status ID must be positive")
        Short accountStatusId
) {
}