package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "Phone or email is required")
    private String phoneOrEmail;

    @NotBlank(message = "Delivery channel is required")
    @Pattern(
            regexp = "(?i)EMAIL|SMS",
            message = "Delivery channel must be EMAIL or SMS"
    )
    private String deliveryChannel;
}