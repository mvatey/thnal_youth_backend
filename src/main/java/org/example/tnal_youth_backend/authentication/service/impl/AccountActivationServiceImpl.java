package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.request.SendActivationOtpRequest;
import org.example.tnal_youth_backend.authentication.model.request.SetActivationPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.VerifyActivationOtpRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;
import org.example.tnal_youth_backend.authentication.repository.PasswordResetTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.service.AccountActivationService;
import org.example.tnal_youth_backend.authentication.service.OtpDeliveryService;
import org.example.tnal_youth_backend.authentication.service.OtpGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountActivationServiceImpl
        implements AccountActivationService {

    private final UserRepository userRepository;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final OtpGenerator
            otpGenerator;

    private final OtpDeliveryService
            emailOtpDeliveryService;
    @Override
    public ApiResponse sendActivationOtp(
            SendActivationOtpRequest request
    ) {

        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }

    @Override
    public ApiResponse verifyActivationOtp(
            VerifyActivationOtpRequest request
    ) {

        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }

    @Override
    public ApiResponse setInitialPassword(
            SetActivationPasswordRequest request
    ) {

        throw new UnsupportedOperationException(
                "Not implemented yet"
        );
    }
}