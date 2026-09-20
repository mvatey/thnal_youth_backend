package org.example.tnal_youth_backend.authentication.model.request;

import jakarta.validation.constraints.NotBlank;
import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;

public record SendActivationOtpRequest(

        @NotBlank(
                message = "Phone number, email, or username is required"
        )
        String phoneOrEmail,

        // Optional -- null means EMAIL (the existing default). Only used
        // for the "didn't get it, resend by phone" option; anything other
        // than SMS is treated as EMAIL.
        OtpChannel deliveryChannel
) {
}
