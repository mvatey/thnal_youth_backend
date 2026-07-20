package org.example.tnal_youth_backend.authentication.service.impl;

import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;
import org.example.tnal_youth_backend.authentication.service.OtpDeliveryService;
import org.example.tnal_youth_backend.authentication.service.OtpSender;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class OtpSenderImpl implements OtpSender {

    private final OtpDeliveryService emailOtpDeliveryService;
    private final OtpDeliveryService smsOtpDeliveryService;

    public OtpSenderImpl(
            @Qualifier("emailOtpDeliveryService")
            OtpDeliveryService emailOtpDeliveryService,

            @Qualifier("smsOtpDeliveryService")
            OtpDeliveryService smsOtpDeliveryService
    ) {
        this.emailOtpDeliveryService = emailOtpDeliveryService;
        this.smsOtpDeliveryService = smsOtpDeliveryService;
    }

    @Override
    public void send(
            OtpChannel channel,
            String destination,
            String otpCode
    ) {
        if (channel == OtpChannel.EMAIL) {
            emailOtpDeliveryService.sendOtp(
                    destination,
                    otpCode
            );
            return;
        }

        if (channel == OtpChannel.SMS) {
            smsOtpDeliveryService.sendOtp(
                    destination,
                    otpCode
            );
            return;
        }

        throw new IllegalArgumentException(
                "Unsupported OTP delivery channel"
        );
    }
}