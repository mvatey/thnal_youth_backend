package org.example.tnal_youth_backend.member.password.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberPasswordResetRequest {

    @NotBlank(
            message = "New password is required"
    )
    @Size(
            min = 6,
            max = 100,
            message = "Password must contain at least 6 characters"
    )
    @JsonProperty("new_password")
    private String newPassword;

    @NotBlank(
            message = "Password confirmation is required"
    )
    @JsonProperty("confirm_password")
    private String confirmPassword;
}