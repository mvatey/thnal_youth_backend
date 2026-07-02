package org.example.tnal_youth_backend.model.request;



import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Phone or Email is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

}