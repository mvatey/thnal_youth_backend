package org.example.tnal_youth_backend.authentication.model.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Size(max = 36, message = "Refresh token format is invalid")
    private String refreshToken;

}
