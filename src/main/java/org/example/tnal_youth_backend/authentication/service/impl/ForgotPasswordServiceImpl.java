package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.PasswordResetToken;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.model.request.ForgotPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.ResetPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;
import org.example.tnal_youth_backend.authentication.repository.PasswordResetTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.service.ForgotPasswordService;
import org.example.tnal_youth_backend.authentication.service.OtpSender;
import org.example.tnal_youth_backend.authentication.util.PhoneNumberUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpSender otpSender;

    @Value("${otp.expire-minutes:5}")
    private long otpExpireMinutes;

    @Value("${otp.cooldown-seconds:60}")
    private long otpCooldownSeconds;

    @Value("${otp.max-per-hour:5}")
    private long maxOtpRequestsPerHour;

    @Value("${otp.max-per-day:10}")
    private long maxOtpRequestsPerDay;

    @Value("${otp.max-attempts:5}")
    private int maxOtpAttempts;

    // =========================================================
    // REQUEST PASSWORD RESET OTP
    // =========================================================

    @Override
    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {

        validateForgotPasswordRequest(request);

        String submittedIdentifier =
                request.getPhoneOrEmail().trim();

        OtpChannel channel =
                request.getDeliveryChannel();

        String lookupIdentifier =
                normalizeIdentifierForLookup(
                        submittedIdentifier,
                        channel
                );

        Optional<User> optionalUser =
                userRepository.findByEmailOrPhone(
                        lookupIdentifier,
                        lookupIdentifier
                );

        /*
         * Do not reveal whether an account exists.
         */
        if (optionalUser.isEmpty()) {
            return genericOtpResponse();
        }

        User user = optionalUser.get();

        if (user.getStatus() != UserStatus.ACTIVE
                && user.getStatus() != UserStatus.LOCKED) {
            return genericOtpResponse();
        }

        String destination =
                resolveDestination(user, channel);

        OffsetDateTime now =
                OffsetDateTime.now();

        validateCooldown(user.getId(), now);
        validateHourlyLimit(user.getId(), now);
        validateDailyLimit(user.getId(), now);

        /*
         * Invalidate previous active OTPs before issuing a new one.
         */
        passwordResetTokenRepository
                .invalidateAllUnconsumedTokensForUser(
                        user.getId(),
                        now
                );

        String plainOtp =
                generateOtp();

        PasswordResetToken token =
                PasswordResetToken.builder()
                        .user(user)
                        .otpCodeHash(
                                passwordEncoder.encode(plainOtp)
                        )
                        .deliveryChannel(channel)
                        .expiresAt(
                                now.plusMinutes(otpExpireMinutes)
                        )
                        .consumedAt(null)
                        .attempts(0)
                        .createdAt(now)
                        .build();

        passwordResetTokenRepository.save(token);

        otpSender.send(
                channel,
                destination,
                plainOtp
        );

        return genericOtpResponse();
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Override
    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {

        validateResetPasswordRequest(request);

        String submittedIdentifier =
                request.getPhoneOrEmail().trim();

        OtpChannel channel =
                determineChannel(submittedIdentifier);

        String lookupIdentifier =
                normalizeIdentifierForLookup(
                        submittedIdentifier,
                        channel
                );

        User user =
                userRepository.findByEmailOrPhone(
                                lookupIdentifier,
                                lookupIdentifier
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid or expired password reset request"
                                )
                        );

        OffsetDateTime now =
                OffsetDateTime.now();

        PasswordResetToken token =
                passwordResetTokenRepository
                        .findTopByUser_IdAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                                user.getId(),
                                now
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "OTP is invalid or expired"
                                )
                        );

        verifyOtpAttemptsAvailable(token, now);

        boolean otpMatches =
                passwordEncoder.matches(
                        request.getOtp().trim(),
                        token.getOtpCodeHash()
                );

        if (!otpMatches) {
            registerInvalidOtpAttempt(token, now);
        }

        token.setConsumedAt(now);
        passwordResetTokenRepository.save(token);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        userRepository.save(user);

        /*
         * End all existing refresh-token sessions.
         */
        refreshTokenRepository.deleteByUser(user);

        return ApiResponse.builder()
                .success(true)
                .message("Password reset successfully")
                .build();
    }

    // =========================================================
    // REQUEST VALIDATION
    // =========================================================

    private void validateForgotPasswordRequest(
            ForgotPasswordRequest request
    ) {
        if (request == null
                || request.getPhoneOrEmail() == null
                || request.getPhoneOrEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number or email is required"
            );
        }

        if (request.getDeliveryChannel() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Delivery channel is required"
            );
        }
    }

    private void validateResetPasswordRequest(
            ResetPasswordRequest request
    ) {
        if (request == null
                || request.getPhoneOrEmail() == null
                || request.getPhoneOrEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number or email is required"
            );
        }

        if (request.getOtp() == null
                || request.getOtp().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP is required"
            );
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password is required"
            );
        }
    }

    // =========================================================
    // OTP VERIFICATION
    // =========================================================

    private void verifyOtpAttemptsAvailable(
            PasswordResetToken token,
            OffsetDateTime now
    ) {
        int attempts =
                token.getAttempts() == null
                        ? 0
                        : token.getAttempts();

        if (attempts >= maxOtpAttempts) {
            token.setConsumedAt(now);
            passwordResetTokenRepository.save(token);

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Maximum OTP verification attempts reached"
            );
        }
    }

    private void registerInvalidOtpAttempt(
            PasswordResetToken token,
            OffsetDateTime now
    ) {
        int currentAttempts =
                token.getAttempts() == null
                        ? 0
                        : token.getAttempts();

        int updatedAttempts =
                currentAttempts + 1;

        token.setAttempts(updatedAttempts);

        if (updatedAttempts >= maxOtpAttempts) {
            token.setConsumedAt(now);
        }

        passwordResetTokenRepository.save(token);

        throw new ResponseStatusException(
                updatedAttempts >= maxOtpAttempts
                        ? HttpStatus.TOO_MANY_REQUESTS
                        : HttpStatus.UNAUTHORIZED,
                updatedAttempts >= maxOtpAttempts
                        ? "Maximum OTP verification attempts reached"
                        : "Invalid OTP"
        );
    }

    // =========================================================
    // RATE LIMITS
    // =========================================================

    private void validateCooldown(
            Long userId,
            OffsetDateTime now
    ) {
        passwordResetTokenRepository
                .findTopByUser_IdOrderByCreatedAtDesc(userId)
                .ifPresent(latestToken -> {

                    OffsetDateTime nextAllowedTime =
                            latestToken.getCreatedAt()
                                    .plusSeconds(otpCooldownSeconds);

                    if (now.isBefore(nextAllowedTime)) {
                        long secondsRemaining =
                                Math.max(
                                        1,
                                        Duration.between(
                                                now,
                                                nextAllowedTime
                                        ).getSeconds()
                                );

                        throw new ResponseStatusException(
                                HttpStatus.TOO_MANY_REQUESTS,
                                "Please wait "
                                        + secondsRemaining
                                        + " seconds before requesting another OTP"
                        );
                    }
                });
    }

    private void validateHourlyLimit(
            Long userId,
            OffsetDateTime now
    ) {
        long requestsLastHour =
                passwordResetTokenRepository
                        .countByUser_IdAndCreatedAtAfter(
                                userId,
                                now.minusHours(1)
                        );

        if (requestsLastHour >= maxOtpRequestsPerHour) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many OTP requests. Please try again later."
            );
        }
    }

    private void validateDailyLimit(
            Long userId,
            OffsetDateTime now
    ) {
        long requestsLastDay =
                passwordResetTokenRepository
                        .countByUser_IdAndCreatedAtAfter(
                                userId,
                                now.minusHours(24)
                        );

        if (requestsLastDay >= maxOtpRequestsPerDay) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Daily OTP request limit reached"
            );
        }
    }

    // =========================================================
    // IDENTIFIER AND CHANNEL
    // =========================================================

    private OtpChannel determineChannel(
            String phoneOrEmail
    ) {
        return phoneOrEmail.contains("@")
                ? OtpChannel.EMAIL
                : OtpChannel.SMS;
    }

    private String normalizeIdentifierForLookup(
            String identifier,
            OtpChannel channel
    ) {
        if (channel == OtpChannel.EMAIL) {
            return identifier
                    .trim()
                    .toLowerCase();
        }

        return PhoneNumberUtil.toDatabaseFormat(identifier);
    }

    // =========================================================
    // DELIVERY DESTINATION
    // =========================================================

    private String resolveDestination(
            User user,
            OtpChannel channel
    ) {
        if (channel == OtpChannel.EMAIL) {
            if (user.getEmail() == null
                    || user.getEmail().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "This account does not have an email address"
                );
            }

            return user.getEmail()
                    .trim()
                    .toLowerCase();
        }

        if (user.getPhone() == null
                || user.getPhone().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account does not have a phone number"
            );
        }

        return PhoneNumberUtil.toSmsFormat(
                user.getPhone()
        );
    }

    // =========================================================
    // OTP GENERATION AND RESPONSE
    // =========================================================

    private String generateOtp() {
        int number =
                SECURE_RANDOM.nextInt(1_000_000);

        return String.format(
                "%06d",
                number
        );
    }

    private ApiResponse genericOtpResponse() {
        return ApiResponse.builder()
                .success(true)
                .message(
                        "If the account exists, a verification code has been sent."
                )
                .build();
    }
}