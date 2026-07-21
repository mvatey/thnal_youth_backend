package org.example.tnal_youth_backend.authentication.service;

public interface OtpDeliveryService {

    void sendOtp(
            String destination,
            String otpCode
    );
}