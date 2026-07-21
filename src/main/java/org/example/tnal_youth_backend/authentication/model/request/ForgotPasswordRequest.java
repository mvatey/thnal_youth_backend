package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "Phone or email is required")
    private String phoneOrEmail;

    @NotNull(message = "Delivery channel is required")
    private OtpChannel deliveryChannel;

}