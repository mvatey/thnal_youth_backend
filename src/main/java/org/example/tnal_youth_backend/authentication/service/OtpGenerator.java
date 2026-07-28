package org.example.tnal_youth_backend.authentication.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private static final SecureRandom
            SECURE_RANDOM = new SecureRandom();

    public String generate() {

        int number =
                SECURE_RANDOM.nextInt(1_000_000);

        return String.format(
                "%06d",
                number
        );
    }
}