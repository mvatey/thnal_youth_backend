package org.example.tnal_youth_backend.authentication.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponse {

    private boolean success;

    private String message;
}