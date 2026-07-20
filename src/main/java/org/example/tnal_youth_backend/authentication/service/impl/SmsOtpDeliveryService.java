package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.service.OtpDeliveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.example.tnal_youth_backend.authentication.util.PhoneNumberUtil;

import java.util.Map;

@Service("smsOtpDeliveryService")
@Slf4j
public class SmsOtpDeliveryService implements OtpDeliveryService {

    private final RestClient restClient;
    private final String privateKey;
    private final String secretKey;
    private final String sender;
    private final long otpExpireMinutes;

    public SmsOtpDeliveryService(
            @Value("${plasgate.base-url}")
            String baseUrl,

            @Value("${plasgate.private-key}")
            String privateKey,

            @Value("${plasgate.secret-key}")
            String secretKey,

            @Value("${plasgate.sender}")
            String sender,

            @Value("${otp.expire-minutes:5}")
            long otpExpireMinutes
    ) {
        if (baseUrl == null
                || baseUrl.isBlank()
                || !baseUrl.startsWith("http")) {
            throw new IllegalArgumentException(
                    "Invalid PlasGate base URL: " + baseUrl
            );
        }

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.privateKey = privateKey;
        this.secretKey = secretKey;
        this.sender = sender;
        this.otpExpireMinutes = otpExpireMinutes;
    }

    @Override
    public void sendOtp(
            String destination,
            String otpCode
    ) {
        String normalizedDestination =
                PhoneNumberUtil.toSmsFormat(destination);

        String content = """
                TNAL Youth verification code: #ma#%s#ma#
                This code expires in %d minutes.
                Do not share this code with anyone.
                """.formatted(
                otpCode,
                otpExpireMinutes
        );

        Map<String, String> requestBody = Map.of(
                "sender", sender,
                "to", normalizedDestination,
                "content", content
        );

        try {
            restClient
                    .post()
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path("/rest/send")
                                    .queryParam(
                                            "private_key",
                                            privateKey
                                    )
                                    .build()
                    )
                    .header(
                            "X-Secret",
                            secretKey
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {
            log.error(
                    "PlasGate SMS failed for phone {}",
                    maskPhone(normalizedDestination),
                    exception
            );

            throw new IllegalStateException(
                    "Unable to send password reset SMS",
                    exception
            );
        }
    }

    private String normalizeDestination(
            String destination
    ) {
        if (destination == null
                || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "SMS destination is required"
            );
        }

        String cleaned =
                destination.replaceAll("[^0-9+]", "");

        if (cleaned.startsWith("+855")) {
            return cleaned.substring(1);
        }

        if (cleaned.startsWith("855")) {
            return cleaned;
        }

        if (cleaned.startsWith("0")) {
            return "855" + cleaned.substring(1);
        }

        throw new IllegalArgumentException(
                "Invalid Cambodian phone number"
        );
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }

        return phone.substring(0, 3)
                + "*****"
                + phone.substring(phone.length() - 3);
    }
}
