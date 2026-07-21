package org.example.tnal_youth_backend.authentication.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {

    private boolean success;

    private String message;

    private String deliveryChannel;

    private String destination;
}