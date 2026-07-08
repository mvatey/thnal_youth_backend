package org.example.tnal_youth_backend.authentication.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.LoginHistory;
import org.example.tnal_youth_backend.authentication.model.entity.RefreshToken;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.model.request.LoginRequest;
import org.example.tnal_youth_backend.authentication.model.request.RefreshTokenRequest;
import org.example.tnal_youth_backend.authentication.model.response.ApiResponse;
import org.example.tnal_youth_backend.authentication.model.response.LoginResponse;
import org.example.tnal_youth_backend.authentication.model.response.RefreshTokenResponse;
import org.example.tnal_youth_backend.authentication.repository.LoginHistoryRepository;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {

        User user = userRepository.findByPhone(request.getPhoneOrEmail())
                .or(() -> userRepository.findByEmail(request.getPhoneOrEmail()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        unlockIfExpired(user);

        if (user.getStatus() == UserStatus.LOCKED) {
            recordLoginHistory(user, httpRequest, false);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Account is locked. Try again after " + user.getLockedUntil());
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            recordLoginHistory(user, httpRequest, false);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            recordLoginHistory(user, httpRequest, false);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid phone/email or password");
        }

        handleSuccessfulLogin(user);
        recordLoginHistory(user, httpRequest, true);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {

        UUID tokenValue = parseToken(request.getRefreshToken());

        RefreshToken existing = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (Boolean.TRUE.equals(existing.getRevoked())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
        }

        if (existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        User user = existing.getUser();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        // Rotation: old token is burned the moment it's used
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        String newAccessToken = jwtService.generateToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken().toString())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse logout(RefreshTokenRequest request) {

        UUID tokenValue = parseToken(request.getRefreshToken());

        refreshTokenRepository.findByToken(tokenValue).ifPresent(existing -> {
            if (!Boolean.TRUE.equals(existing.getRevoked())) {
                existing.setRevoked(true);
                refreshTokenRepository.save(existing);
            }
        });

        return ApiResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void unlockIfExpired(User user) {
        if (user.getStatus() == UserStatus.LOCKED
                && user.getLockedUntil() != null
                && user.getLockedUntil().isBefore(OffsetDateTime.now())) {

            user.setStatus(UserStatus.ACTIVE);
            user.setLockedUntil(null);
            user.setFailedLoginCount(0);
            userRepository.save(user);
        }
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount();
        attempts++;
        user.setFailedLoginCount(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(OffsetDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }

        userRepository.save(user);
    }

    private void handleSuccessfulLogin(User user) {
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(OffsetDateTime.now());
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID())
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private void recordLoginHistory(User user, HttpServletRequest httpRequest, boolean success) {
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .loginTime(OffsetDateTime.now())
                .ipAddress(extractIp(httpRequest))
                .browser(httpRequest.getHeader("User-Agent"))
                .device(extractDevice(httpRequest))
                .success(success)
                .build();

        loginHistoryRepository.save(history);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile")) return "Mobile";
        if (ua.contains("tablet")) return "Tablet";
        return "Desktop";
    }

    private UUID parseToken(String token) {
        try {
            return UUID.fromString(token);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token format");
        }
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, RefreshToken refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken().toString())
                .userId(user.getId())
                .fullName(user.getFullNameEn())
                .role(UserRole.valueOf(user.getRole().name()))
                .build();
    }
}