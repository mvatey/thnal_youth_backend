package org.example.tnal_youth_backend.account.myaccount.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateMyAccountRequest(

        @Size(
                max = 30,
                message = "Phone number must not exceed 30 characters"
        )
        String phone,

        @Email(message = "Email format is invalid")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email

) {
}