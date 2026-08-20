package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.service.OtpDeliveryService;
import org.example.tnal_youth_backend.authentication.util.PhoneNumberUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service("smsOtpDeliveryService")
@Slf4j
public class SmsOtpDeliveryService
        implements OtpDeliveryService {

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

        validateConfiguration(
                baseUrl,
                privateKey,
                secretKey,
                sender
        );

        /*
         * Example:
         *
         * plasgate.base-url=
         * https://cloudapi.plasgate.com
         *
         * Then:
         *
         * POST
         * https://cloudapi.plasgate.com/rest/send
         */
        this.restClient =
                RestClient.builder()
                        .baseUrl(
                                baseUrl.trim()
                        )
                        .build();

        this.privateKey =
                privateKey.trim();

        this.secretKey =
                secretKey.trim();

        this.sender =
                sender.trim();

        this.otpExpireMinutes =
                otpExpireMinutes;
    }

    @Override
    public void sendOtp(
            String destination,
            String otpCode
    ) {
        String normalizedDestination =
                PhoneNumberUtil.toSmsFormat(
                        destination
                );

        if (normalizedDestination == null
                || normalizedDestination.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid SMS destination"
            );
        }

        if (otpCode == null
                || otpCode.isBlank()) {
            throw new IllegalArgumentException(
                    "OTP code is required"
            );
        }

        /*
         * TEMPORARY simple message for testing.
         *
         * Do not use #ma# yet.
         */
        String content =
                "TNAL Youth OTP: " + otpCode;

        Map<String, String> requestBody =
                Map.of(
                        "sender",
                        sender,

                        "to",
                        normalizedDestination,

                        "content",
                        content
                );

        try {
            log.info(
                    "Sending PlasGate SMS: "
                            + "phone={}, sender={}, contentLength={}",
                    maskPhone(
                            normalizedDestination
                    ),
                    sender,
                    content.length()
            );

            restClient
                    .post()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path(
                                                    "/rest/send"
                                            )
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
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .accept(
                            MediaType.APPLICATION_JSON
                    )
                    .body(
                            requestBody
                    )
                    .retrieve()
                    .toBodilessEntity();

            log.info(
                    "PlasGate SMS sent successfully to {}",
                    maskPhone(
                            normalizedDestination
                    )
            );

        } catch (
                RestClientResponseException exception
        ) {
            log.error(
                    "PlasGate rejected SMS. "
                            + "status={}, response={}, "
                            + "phone={}, sender={}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    maskPhone(
                            normalizedDestination
                    ),
                    sender,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to send password reset SMS. "
                            + "PlasGate returned HTTP "
                            + exception
                            .getStatusCode()
                            .value(),
                    exception
            );

        } catch (
                RestClientException exception
        ) {
            log.error(
                    "Could not connect to PlasGate. phone={}",
                    maskPhone(
                            normalizedDestination
                    ),
                    exception
            );

            throw new IllegalStateException(
                    "Unable to connect to SMS provider",
                    exception
            );
        }
    }

    private void validateConfiguration(
            String baseUrl,
            String privateKey,
            String secretKey,
            String sender
    ) {

        if (baseUrl == null
                || baseUrl.isBlank()) {

            throw new IllegalArgumentException(
                    "plasgate.base-url is required"
            );
        }

        if (!baseUrl.startsWith(
                "http://"
        )
                &&
                !baseUrl.startsWith(
                        "https://"
                )) {

            throw new IllegalArgumentException(
                    "Invalid PlasGate base URL: "
                            + baseUrl
            );
        }

        if (privateKey == null
                || privateKey.isBlank()) {

            throw new IllegalArgumentException(
                    "plasgate.private-key is required"
            );
        }

        if (secretKey == null
                || secretKey.isBlank()) {

            throw new IllegalArgumentException(
                    "plasgate.secret-key is required"
            );
        }

        if (sender == null
                || sender.isBlank()) {

            throw new IllegalArgumentException(
                    "plasgate.sender is required"
            );
        }
    }

    private String maskPhone(
            String phone
    ) {

        if (phone == null
                || phone.length() < 6) {

            return "***";
        }

        return phone.substring(
                0,
                3
        )
                + "*****"
                + phone.substring(
                phone.length() - 3
        );
    }
}