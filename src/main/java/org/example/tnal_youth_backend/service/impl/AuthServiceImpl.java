package org.example.tnal_youth_backend.service.impl;


import org.example.tnal_youth_backend.model.entity.LoginHistory;
import org.example.tnal_youth_backend.model.entity.RefreshToken;
import org.example.tnal_youth_backend.model.entity.User;
import org.example.tnal_youth_backend.model.enums.UserStatus;
import org.example.tnal_youth_backend.model.request.LoginRequest;
import org.example.tnal_youth_backend.model.response.LoginResponse;
import org.example.tnal_youth_backend.repository.LoginHistoryRepository;
import org.example.tnal_youth_backend.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.repository.UserRepository;
import org.example.tnal_youth_backend.service.JwtService;
import org.example.tnal_youth_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByPhoneOrEmail(
                        request.getUsername(),
                        request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Invalid username or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Account is not active.");
        }

        boolean matched =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash());

        if (!matched) {

            saveLoginHistory(user, false);

            throw new RuntimeException("Invalid username or password");
        }

        String accessToken =
                jwtService.generateToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        saveLoginHistory(user, true);

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken().toString())
                .userId(user.getId())
                .fullName(user.getFullNameEn())
                .role(user.getRole())
                .build();
    }

    private void saveLoginHistory(User user, boolean success){

        LoginHistory history = LoginHistory.builder()
                .user(user)
                .success(success)
                .loginTime(OffsetDateTime.now())
                .build();

        loginHistoryRepository.save(history);
    }

}
