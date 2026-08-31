package org.example.tnal_youth_backend.authentication.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.service.OtpDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Sends OTP emails through Resend's HTTP API rather than SMTP, because
 * Railway blocks outbound SMTP ports (25/465/587/2525) below its Pro plan.
 */
@Service("emailOtpDeliveryService")
@RequiredArgsConstructor
@Slf4j
public class EmailOtpDeliveryService implements OtpDeliveryService {

    private static final URI RESEND_API_URI =
            URI.create("https://api.resend.com/emails");

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${resend.api-key}")
    private String resendApiKey;

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

        String textBody = """
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
                );

        Map<String, Object> payload = Map.of(
                "from", fromAddress,
                "to", List.of(normalizedDestination),
                "subject", "TNAL Youth Cambodia password reset code",
                "text", textBody
        );

        try {
            log.info(
                    "Attempting OTP email delivery to {}",
                    maskEmail(normalizedDestination)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(RESEND_API_URI)
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(payload)
                    ))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                log.error(
                        "Resend API returned {} for OTP email to {}: {}",
                        response.statusCode(),
                        maskEmail(normalizedDestination),
                        response.body()
                );

                throw new IllegalStateException(
                        "Unable to send password reset email"
                );
            }

            log.info(
                    "OTP email sent successfully to {}",
                    maskEmail(normalizedDestination)
            );

        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
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
