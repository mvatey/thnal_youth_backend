package org.example.tnal_youth_backend.authentication.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.LoginHistory;
import org.example.tnal_youth_backend.authentication.model.entity.RefreshToken;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.request.ForgotPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.LoginRequest;
import org.example.tnal_youth_backend.authentication.model.request.RefreshTokenRequest;
import org.example.tnal_youth_backend.authentication.model.request.ResetPasswordRequest;
import org.example.tnal_youth_backend.authentication.model.request.VerifyOtpRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;
import org.example.tnal_youth_backend.authentication.model.response.ForgotPasswordResponse;
import org.example.tnal_youth_backend.authentication.model.response.LoginResponse;
import org.example.tnal_youth_backend.authentication.model.response.RefreshTokenResponse;
import org.example.tnal_youth_backend.authentication.model.response.UserProfileResponse;
import org.example.tnal_youth_backend.authentication.model.response.VerifyOtpResponse;
import org.example.tnal_youth_backend.authentication.repository.LoginHistoryRepository;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.authentication.service.AuthService;
import org.example.tnal_youth_backend.authentication.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    private static final String ACTIVE_ACCOUNT_STATUS = "ACTIVE";

    private static final String FAILURE_ACCOUNT_LOCKED =
            "ACCOUNT_LOCKED";

    private static final String FAILURE_ACCOUNT_INACTIVE =
            "ACCOUNT_INACTIVE";

    private static final String FAILURE_INVALID_CREDENTIALS =
            "INVALID_CREDENTIALS";

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final LoginHistoryRepository loginHistoryRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    // =========================================================
    // LOGIN
    // =========================================================

    @Override
    @Transactional
    public LoginResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        validateLoginRequest(request);

        String identifier =
                request.getPhoneOrEmail().trim();

        User user = userRepository
                .findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid phone/email or password"
                        )
                );

        unlockIfExpired(user);

        if (isCurrentlyLocked(user)) {
            recordLoginHistory(
                    user,
                    identifier,
                    httpRequest,
                    false,
                    FAILURE_ACCOUNT_LOCKED
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Account is locked. Try again after "
                            + user.getLockedUntil()
            );
        }

        if (!isActive(user)) {
            recordLoginHistory(
                    user,
                    identifier,
                    httpRequest,
                    false,
                    FAILURE_ACCOUNT_INACTIVE
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not active"
            );
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            handleFailedLogin(user);

            recordLoginHistory(
                    user,
                    identifier,
                    httpRequest,
                    false,
                    FAILURE_INVALID_CREDENTIALS
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid phone/email or password"
            );
        }

        handleSuccessfulLogin(user);

        recordLoginHistory(
                user,
                identifier,
                httpRequest,
                true,
                null
        );

        String accessToken =
                jwtService.generateToken(user);

        RefreshToken refreshToken =
                createRefreshToken(user);

        return buildLoginResponse(
                user,
                accessToken,
                refreshToken
        );
    }

    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Override
    @Transactional
    public RefreshTokenResponse refresh(
            RefreshTokenRequest request
    ) {
        validateRefreshTokenRequest(request);

        UUID tokenValue =
                parseToken(request.getRefreshToken());

        RefreshToken existing =
                refreshTokenRepository
                        .findByToken(tokenValue)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid refresh token"
                                )
                        );

        if (existing.getRevokedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has been revoked"
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now();

        if (existing.getExpiresAt()
                .isBefore(now)) {

            existing.setRevokedAt(now);

            refreshTokenRepository.save(existing);

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired"
            );
        }

        User user = existing.getUser();

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token user was not found"
            );
        }

        unlockIfExpired(user);

        if (isCurrentlyLocked(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is locked"
            );
        }

        if (!isActive(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not active"
            );
        }

        /*
         * Refresh-token rotation:
         * revoke the old token and create a new token.
         */
        existing.setRevokedAt(now);

        refreshTokenRepository.save(existing);

        String newAccessToken =
                jwtService.generateToken(user);

        RefreshToken newRefreshToken =
                createRefreshToken(user);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(
                        newRefreshToken
                                .getToken()
                                .toString()
                )
                .build();
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @Override
    @Transactional
    public ApiResponse logout(
            RefreshTokenRequest request
    ) {
        validateRefreshTokenRequest(request);

        UUID tokenValue =
                parseToken(request.getRefreshToken());

        refreshTokenRepository
                .findByToken(tokenValue)
                .ifPresent(existing -> {

                    if (existing.getRevokedAt() == null) {
                        existing.setRevokedAt(
                                OffsetDateTime.now()
                        );

                        refreshTokenRepository.save(existing);
                    }
                });

        return ApiResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build();
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser() {
        User user = SecurityUtil.getCurrentUser();

        String roleCode = null;

        if (user.getRole() != null) {
            user.getRole().name();
        }

        /*
         * Member fields are temporarily null because Member.java
         * does not exist yet.
         *
         * Later:
         * users.member_id -> members.id
         * members.profile_photo_id -> files.id
         */
        return UserProfileResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .fullNameKm(null)
                .fullNameEn(null)
                .profileImage(null)
                .role(roleCode)
                .build();
    }

    // =========================================================
    // PASSWORD RESET
    // =========================================================

    @Override
    public ForgotPasswordResponse forgotPassword(
            ForgotPasswordRequest request
    ) {
        /*
         * Keep your existing forgot-password implementation here.
         * It was not included in the provided code.
         */
        throw new UnsupportedOperationException(
                "Forgot password is not implemented yet"
        );
    }

    @Override
    public VerifyOtpResponse verifyOtp(
            VerifyOtpRequest request
    ) {
        /*
         * Keep your existing OTP verification implementation here.
         */
        throw new UnsupportedOperationException(
                "OTP verification is not implemented yet"
        );
    }

    @Override
    public ApiResponse resetPassword(
            ResetPasswordRequest request
    ) {
        /*
         * Keep your existing password-reset implementation here.
         */
        throw new UnsupportedOperationException(
                "Password reset is not implemented yet"
        );
    }

    // =========================================================
    // ACCOUNT HELPERS
    // =========================================================

    private boolean isActive(User user) {
        return user.getStatus() != null
                && ACTIVE_ACCOUNT_STATUS.equals(
                user.getStatus().name()
        );
    }

    private boolean isCurrentlyLocked(User user) {
        return user.getLockedUntil() != null
                && user.getLockedUntil()
                .isAfter(OffsetDateTime.now());
    }

    private void unlockIfExpired(User user) {
        OffsetDateTime lockedUntil =
                user.getLockedUntil();

        if (lockedUntil != null
                && !lockedUntil.isAfter(
                OffsetDateTime.now()
        )) {

            user.setLockedUntil(null);
            user.setFailedLoginCount(0);

            userRepository.save(user);
        }
    }

    private void handleFailedLogin(User user) {
        int attempts =
                user.getFailedLoginCount() == null
                        ? 0
                        : user.getFailedLoginCount();

        attempts++;

        user.setFailedLoginCount(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(
                    OffsetDateTime.now()
                            .plusMinutes(
                                    LOCK_DURATION_MINUTES
                            )
            );
        }

        userRepository.save(user);
    }

    private void handleSuccessfulLogin(User user) {
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(
                OffsetDateTime.now()
        );

        userRepository.save(user);
    }

    // =========================================================
    // REFRESH TOKEN HELPERS
    // =========================================================

    private RefreshToken createRefreshToken(
            User user
    ) {
        OffsetDateTime now =
                OffsetDateTime.now();

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .user(user)
                        .token(UUID.randomUUID())
                        .expiresAt(
                                now.plusSeconds(
                                        refreshExpirationMs / 1000
                                )
                        )
                        .revokedAt(null)
                        .build();

        return refreshTokenRepository.save(
                refreshToken
        );
    }

    private UUID parseToken(String token) {
        try {
            return UUID.fromString(token);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid token format"
            );
        }
    }

    // =========================================================
    // LOGIN HISTORY
    // =========================================================

    private void recordLoginHistory(
            User user,
            String loginIdentifier,
            HttpServletRequest request,
            boolean success,
            String failureReason
    ) {
        LoginHistory history =
                LoginHistory.builder()
                        .user(user)
                        .loginIdentifier(
                                loginIdentifier
                        )
                        .success(success)
                        .failureReason(
                                failureReason
                        )
                        .ipAddress(
                                extractIp(request)
                        )
                        .userAgent(
                                request.getHeader(
                                        "User-Agent"
                                )
                        )
                        .build();

        loginHistoryRepository.save(history);
    }

    private String extractIp(
            HttpServletRequest request
    ) {
        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        String realIp =
                request.getHeader("X-Real-IP");

        if (realIp != null
                && !realIp.isBlank()) {

            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    // =========================================================
    // RESPONSE BUILDERS
    // =========================================================

    private LoginResponse buildLoginResponse(
            User user,
            String accessToken,
            RefreshToken refreshToken
    ) {
        String roleCode = null;

        if (user.getRole() != null) {
            roleCode = user.getRole().name();
        }

        /*
         * fullName is temporarily null until Member.java exists.
         *
         * Later:
         * user.getMember().getFullNameEn()
         */
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(
                        refreshToken
                                .getToken()
                                .toString()
                )
                .userId(user.getId())
                .fullName(null)
                .role(roleCode)
                .build();
    }

    // =========================================================
    // REQUEST VALIDATION
    // =========================================================

    private void validateLoginRequest(
            LoginRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Login request is required"
            );
        }

        if (request.getPhoneOrEmail() == null
                || request.getPhoneOrEmail()
                .isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number or email is required"
            );
        }

        if (request.getPassword() == null
                || request.getPassword()
                .isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is required"
            );
        }
    }

    private void validateRefreshTokenRequest(
            RefreshTokenRequest request
    ) {
        if (request == null
                || request.getRefreshToken() == null
                || request.getRefreshToken()
                .isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Refresh token is required"
            );
        }
    }
}