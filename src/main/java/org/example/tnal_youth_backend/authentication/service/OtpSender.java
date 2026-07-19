package org.example.tnal_youth_backend.authentication.service;

import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;

public interface OtpSender {

    void send(
            OtpChannel channel,
            String destination,
            String otpCode
    );
}