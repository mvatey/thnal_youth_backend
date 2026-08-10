package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.PasswordResetToken;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;
import org.example.tnal_youth_backend.authentication.model.enums.OtpPurpose;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.model.request.SendActivationOtpRequest;
import org.example.tnal_youth_backend.authentication.model.request.SetActivationPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.VerifyActivationOtpRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;
import org.example.tnal_youth_backend.authentication.repository.PasswordResetTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.service.AccountActivationService;
import org.example.tnal_youth_backend.authentication.service.OtpGenerator;
import org.example.tnal_youth_backend.authentication.service.OtpSender;
import org.example.tnal_youth_backend.authentication.util.PhoneNumberUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AccountActivationServiceImpl
        implements AccountActivationService {

    private final UserRepository userRepository;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final RefreshTokenRepository
            refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final OtpGenerator otpGenerator;

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

    /*
     * ==========================================================
     * SEND ACTIVATION OTP
     * ==========================================================
     */

    @Override
    @Transactional
    public ApiResponse sendActivationOtp(
            SendActivationOtpRequest request
    ) {
        validateIdentifier(
                request == null
                        ? null
                        : request.phoneOrEmail()
        );

        User user =
                findPendingUser(
                        request.phoneOrEmail()
                );

        String destination =
                requireEmail(user);

        OffsetDateTime now =
                OffsetDateTime.now();

        validateCooldown(
                user.getId(),
                now
        );

        validateHourlyLimit(
                user.getId(),
                now
        );

        validateDailyLimit(
                user.getId(),
                now
        );

        passwordResetTokenRepository
                .invalidateAllUnconsumedTokensForUserAndPurpose(
                        user.getId(),
                        OtpPurpose.ACCOUNT_ACTIVATION,
                        now
                );

        String plainOtp =
                otpGenerator.generate();

        PasswordResetToken token =
                PasswordResetToken.builder()
                        .user(user)
                        .purpose(
                                OtpPurpose.ACCOUNT_ACTIVATION
                        )
                        .otpCodeHash(
                                passwordEncoder.encode(
                                        plainOtp
                                )
                        )
                        .deliveryChannel(
                                OtpChannel.EMAIL
                        )
                        .expiresAt(
                                now.plusMinutes(
                                        otpExpireMinutes
                                )
                        )
                        .consumedAt(null)
                        .attempts(0)
                        .createdAt(now)
                        .build();

        passwordResetTokenRepository.save(
                token
        );

        otpSender.send(
                OtpChannel.EMAIL,
                destination,
                plainOtp
        );

        return ApiResponse.builder()
                .success(true)
                .message(
                        "An account activation code has been sent."
                )
                .build();
    }

    /*
     * ==========================================================
     * VERIFY ACTIVATION OTP
     * ==========================================================
     */

    @Override
    @Transactional
    public ApiResponse verifyActivationOtp(
            VerifyActivationOtpRequest request
    ) {
        validateIdentifier(
                request == null
                        ? null
                        : request.phoneOrEmail()
        );

        validateOtp(
                request == null
                        ? null
                        : request.otp()
        );

        User user =
                findPendingUser(
                        request.phoneOrEmail()
                );

        OffsetDateTime now =
                OffsetDateTime.now();

        PasswordResetToken token =
                requireActiveActivationToken(
                        user.getId(),
                        now
                );

        verifyOtpAttemptsAvailable(
                token,
                now
        );

        boolean matches =
                passwordEncoder.matches(
                        request.otp().trim(),
                        token.getOtpCodeHash()
                );

        if (!matches) {
            registerInvalidOtpAttempt(
                    token,
                    now
            );
        }

        /*
         * Do not consume the token yet.
         * The set-password endpoint verifies it again
         * and consumes it after saving the password.
         */
        return ApiResponse.builder()
                .success(true)
                .message(
                        "Activation code verified successfully"
                )
                .build();
    }

    /*
     * ==========================================================
     * SET FIRST PASSWORD
     * ==========================================================
     */

    @Override
    @Transactional
    public ApiResponse setInitialPassword(
            SetActivationPasswordRequest request
    ) {
        validateIdentifier(
                request == null
                        ? null
                        : request.phoneOrEmail()
        );

        validateOtp(
                request == null
                        ? null
                        : request.otp()
        );

        validateNewPassword(
                request == null
                        ? null
                        : request.newPassword()
        );

        User user =
                findPendingUser(
                        request.phoneOrEmail()
                );

        OffsetDateTime now =
                OffsetDateTime.now();

        PasswordResetToken token =
                requireActiveActivationToken(
                        user.getId(),
                        now
                );

        verifyOtpAttemptsAvailable(
                token,
                now
        );

        boolean matches =
                passwordEncoder.matches(
                        request.otp().trim(),
                        token.getOtpCodeHash()
                );

        if (!matches) {
            registerInvalidOtpAttempt(
                    token,
                    now
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        user.setStatus(
                UserStatus.ACTIVE
        );

        user.setActivatedAt(now);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        userRepository.saveAndFlush(
                user
        );

        token.setConsumedAt(now);

        passwordResetTokenRepository.save(
                token
        );

        passwordResetTokenRepository
                .invalidateAllUnconsumedTokensForUserAndPurpose(
                        user.getId(),
                        OtpPurpose.ACCOUNT_ACTIVATION,
                        now
                );

        /*
         * Ensure no old sessions remain.
         */
        refreshTokenRepository.deleteByUser(
                user
        );

        return ApiResponse.builder()
                .success(true)
                .message(
                        "Account activated successfully"
                )
                .build();
    }

    /*
     * ==========================================================
     * USER LOOKUP
     * ==========================================================
     */

    private User findPendingUser(
            String phoneOrEmail
    ) {
        String normalizedIdentifier =
                normalizeIdentifier(
                        phoneOrEmail
                );

        User user =
                userRepository
                        .findByEmailOrPhone(
                                normalizedIdentifier,
                                normalizedIdentifier
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid account activation request"
                                )
                        );

        if (user.getStatus()
                != UserStatus.PENDING_ACTIVATION) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is not pending activation"
            );
        }

        if (user.getActivatedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account has already been activated"
            );
        }

        return user;
    }

    private String requireEmail(
            User user
    ) {
        String email =
                user.getEmail();

        if (email == null
                || email.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account does not have an email address"
            );
        }

        return email
                .trim()
                .toLowerCase();
    }

    /*
     * ==========================================================
     * TOKEN LOOKUP
     * ==========================================================
     */

    private PasswordResetToken
    requireActiveActivationToken(
            Long userId,
            OffsetDateTime now
    ) {
        return passwordResetTokenRepository
                .findTopByUser_IdAndPurposeAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                        userId,
                        OtpPurpose.ACCOUNT_ACTIVATION,
                        now
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Activation code is invalid or expired"
                        )
                );
    }

    /*
     * ==========================================================
     * OTP ATTEMPTS
     * ==========================================================
     */

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

            passwordResetTokenRepository.save(
                    token
            );

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
        int attempts =
                token.getAttempts() == null
                        ? 0
                        : token.getAttempts();

        int updatedAttempts =
                attempts + 1;

        token.setAttempts(
                updatedAttempts
        );

        if (updatedAttempts >= maxOtpAttempts) {
            token.setConsumedAt(now);
        }

        passwordResetTokenRepository.save(
                token
        );

        throw new ResponseStatusException(
                updatedAttempts >= maxOtpAttempts
                        ? HttpStatus.TOO_MANY_REQUESTS
                        : HttpStatus.UNAUTHORIZED,

                updatedAttempts >= maxOtpAttempts
                        ? "Maximum OTP verification attempts reached"
                        : "Invalid OTP"
        );
    }

    /*
     * ==========================================================
     * RATE LIMITS
     * ==========================================================
     */

    private void validateCooldown(
            Long userId,
            OffsetDateTime now
    ) {
        passwordResetTokenRepository
                .findTopByUser_IdAndPurposeOrderByCreatedAtDesc(
                        userId,
                        OtpPurpose.ACCOUNT_ACTIVATION
                )
                .ifPresent(latestToken -> {

                    OffsetDateTime nextAllowedAt =
                            latestToken
                                    .getCreatedAt()
                                    .plusSeconds(
                                            otpCooldownSeconds
                                    );

                    if (now.isBefore(
                            nextAllowedAt
                    )) {
                        long secondsRemaining =
                                Math.max(
                                        1,
                                        Duration.between(
                                                now,
                                                nextAllowedAt
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
        long count =
                passwordResetTokenRepository
                        .countByUser_IdAndPurposeAndCreatedAtAfter(
                                userId,
                                OtpPurpose.ACCOUNT_ACTIVATION,
                                now.minusHours(1)
                        );

        if (count >= maxOtpRequestsPerHour) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many activation-code requests"
            );
        }
    }

    private void validateDailyLimit(
            Long userId,
            OffsetDateTime now
    ) {
        long count =
                passwordResetTokenRepository
                        .countByUser_IdAndPurposeAndCreatedAtAfter(
                                userId,
                                OtpPurpose.ACCOUNT_ACTIVATION,
                                now.minusHours(24)
                        );

        if (count >= maxOtpRequestsPerDay) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Daily activation-code limit reached"
            );
        }
    }

    /*
     * ==========================================================
     * REQUEST VALIDATION
     * ==========================================================
     */

    private void validateIdentifier(
            String phoneOrEmail
    ) {
        if (phoneOrEmail == null
                || phoneOrEmail.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number or email is required"
            );
        }
    }

    private void validateOtp(
            String otp
    ) {
        if (otp == null
                || otp.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP is required"
            );
        }
    }

    private void validateNewPassword(
            String newPassword
    ) {
        if (newPassword == null
                || newPassword.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password is required"
            );
        }

        if (newPassword.length() < 8
                || newPassword.length() > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain between 8 and 72 characters"
            );
        }
    }

    /*
     * ==========================================================
     * NORMALIZATION
     * ==========================================================
     */

    private String normalizeIdentifier(
            String identifier
    ) {
        String trimmed =
                identifier.trim();

        if (trimmed.contains("@")) {
            return trimmed.toLowerCase();
        }

        return PhoneNumberUtil.toDatabaseFormat(
                trimmed
        );
    }
}
