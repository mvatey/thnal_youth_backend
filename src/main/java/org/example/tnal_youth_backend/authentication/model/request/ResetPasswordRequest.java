package org.example.tnal_youth_backend.authentication.model.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    private String otp;

    @NotBlank
    private String newPassword;

}