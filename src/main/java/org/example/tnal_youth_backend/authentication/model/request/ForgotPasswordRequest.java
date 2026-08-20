package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "Phone or email is required")
    @Size(max = 255, message = "Phone or email must not exceed 255 characters")
    private String phoneOrEmail;

    @NotNull(message = "Delivery channel is required")
    private OtpChannel deliveryChannel;

}
