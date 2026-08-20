package org.example.tnal_youth_backend.authentication.model.request;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Phone or Email is required")
    @Size(max = 255, message = "Phone or Email must not exceed 255 characters")
    private String phoneOrEmail;

    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password must not exceed 255 characters")
    private String password;

}
