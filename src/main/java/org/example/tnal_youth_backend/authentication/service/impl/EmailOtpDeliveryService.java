package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.service.OtpDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("emailOtpDeliveryService")
@RequiredArgsConstructor
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
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(destination);
        message.setSubject("TNAL Youth Cambodia password reset code");

        message.setText(
                """
                Hello,

                Your TNAL Youth password reset verification code is:

                %s

                This code expires in %d minutes.

                Do not share this code with anyone.
                If you did not request a password reset, you can ignore this email.

                TNAL Youth
                """.formatted(
                        otpCode,
                        otpExpireMinutes
                )
        );

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new IllegalStateException(
                    "Unable to send password reset email",
                    exception
            );
        }
    }
}