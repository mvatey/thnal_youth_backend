package org.example.tnal_youth_backend.member.password.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberPasswordResetRequest {

    @JsonProperty("newPassword")
    @JsonAlias("new_password")
    @NotBlank(message = "New password is required")
    @Size(
            min = 6,
            message = "Password must contain at least 6 characters"
    )
    private String newPassword;

    @JsonProperty("confirmPassword")
    @JsonAlias("confirm_password")
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
