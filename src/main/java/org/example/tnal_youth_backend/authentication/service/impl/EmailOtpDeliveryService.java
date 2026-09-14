package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.service.OtpDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("emailOtpDeliveryService")
@RequiredArgsConstructor
@Slf4j
public class EmailOtpDeliveryService implements OtpDeliveryService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${otp.expire-minutes:5}")
    private long otpExpireMinutes;

    @Override
    public void sendOtp(
            String destination,
            String otpCode
    ) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Email destination is required"
            );
        }

        if (otpCode == null || otpCode.isBlank()) {
            throw new IllegalArgumentException(
                    "OTP code is required"
            );
        }

        String normalizedDestination =
                destination.trim().toLowerCase();

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(normalizedDestination);
        message.setSubject(
                "TNAL Youth Cambodia password reset code"
        );

        message.setText(
                """
                Hello,

                Your TNAL Youth password reset verification code is:

                %s

                This code expires in %d minutes.

                Do not share this code with anyone.

                If you did not request a password reset,
                you can safely ignore this email.

                TNAL Youth
                """.formatted(
                        otpCode,
                        otpExpireMinutes
                )
        );

        try {
            log.info(
                    "Attempting OTP email delivery to {}",
                    maskEmail(normalizedDestination)
            );

            mailSender.send(message);

            log.info(
                    "OTP email sent successfully to {}",
                    maskEmail(normalizedDestination)
            );

        } catch (MailException exception) {
            log.error(
                    "Failed to send OTP email to {}",
                    maskEmail(normalizedDestination),
                    exception
            );

            throw new IllegalStateException(
                    "Unable to send password reset email",
                    exception
            );
        }
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");

        if (atIndex <= 1) {
            return "***";
        }

        return email.charAt(0)
                + "***"
                + email.substring(atIndex);
    }
}