package org.example.tnal_youth_backend.member.password.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberPasswordResetRequest {

    @JsonProperty("newPassword")
    @JsonAlias("new_password")
    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "\\d{6}",
            message = "Password must contain exactly 6 digits"
    )
    private String newPassword;

    @NotBlank(
            message = "Password confirmation is required"
    )
    @JsonProperty("confirm_password")
    private String confirmPassword;
}
