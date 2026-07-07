package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.LoginHistory;
import org.example.tnal_youth_backend.authentication.model.entity.RefreshToken;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.model.request.LoginRequest;
import org.example.tnal_youth_backend.authentication.model.response.LoginResponse;
import org.example.tnal_youth_backend.authentication.repository.LoginHistoryRepository;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.service.AuthService;
import org.example.tnal_youth_backend.authentication.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        User user = userRepository.findByPhone(request.getPhoneOrEmail())
                .or(() -> userRepository.findByEmail(request.getPhoneOrEmail()))
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is not active"
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid phone/email or password"
            );
        }

        String accessToken = jwtService.generateToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        LoginHistory history = LoginHistory.builder()
                .user(user)
                .success(true)
                .loginTime(OffsetDateTime.now())
                .build();

        loginHistoryRepository.save(history);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken().toString())
                .userId(user.getId())
                .fullName(user.getFullNameEn())
                .role(UserRole.valueOf(user.getRole().name()))
                .build();
    }
}