package org.example.tnal_youth_backend.authentication.service;

import org.example.tnal_youth_backend.authentication.model.entity.RefreshToken;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.model.request.RefreshTokenRequest;
import org.example.tnal_youth_backend.authentication.repository.LoginHistoryRepository;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTokenTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginHistoryRepository loginHistoryRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(
                userRepository,
                refreshTokenRepository,
                loginHistoryRepository,
                passwordEncoder,
                jwtService
        );
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 604_800_000L);
    }

    @Test
    void refresh_rotatesTokenAndRevokesPreviousToken() {
        UUID oldValue = UUID.randomUUID();
        RefreshToken existing = activeToken(oldValue, activeUser());

        when(refreshTokenRepository.findByToken(oldValue))
                .thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(existing.getUser()))
                .thenReturn("new-access-token");

        var response = service.refresh(request(oldValue.toString()));

        assertThat(existing.getRevokedAt()).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNotEqualTo(oldValue.toString());
        verify(refreshTokenRepository).save(existing);
    }

    @Test
    void refresh_rejectsRevokedToken() {
        UUID value = UUID.randomUUID();
        RefreshToken existing = activeToken(value, activeUser());
        existing.setRevokedAt(OffsetDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByToken(value))
                .thenReturn(Optional.of(existing));

        assertStatus(() -> service.refresh(request(value.toString())), HttpStatus.UNAUTHORIZED);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_rejectsExpiredTokenAndPersistsRevocation() {
        UUID value = UUID.randomUUID();
        RefreshToken existing = activeToken(value, activeUser());
        existing.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByToken(value))
                .thenReturn(Optional.of(existing));

        assertStatus(() -> service.refresh(request(value.toString())), HttpStatus.UNAUTHORIZED);

        assertThat(existing.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository).save(existing);
    }

    @Test
    void refresh_rejectsInactiveAccount() {
        UUID value = UUID.randomUUID();
        User user = activeUser();
        user.setStatus(UserStatus.INACTIVE);
        when(refreshTokenRepository.findByToken(value))
                .thenReturn(Optional.of(activeToken(value, user)));

        assertStatus(() -> service.refresh(request(value.toString())), HttpStatus.FORBIDDEN);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_rejectsMalformedTokenBeforeRepositoryLookup() {
        assertStatus(() -> service.refresh(request("not-a-uuid")), HttpStatus.BAD_REQUEST);

        verify(refreshTokenRepository, never()).findByToken(any());
    }

    @Test
    void logout_revokesExistingToken() {
        UUID value = UUID.randomUUID();
        RefreshToken existing = activeToken(value, activeUser());
        when(refreshTokenRepository.findByToken(value))
                .thenReturn(Optional.of(existing));

        var response = service.logout(request(value.toString()));

        assertThat(response.isSuccess()).isTrue();
        assertThat(existing.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository).save(existing);
    }

    private static User activeUser() {
        return User.builder()
                .id(10L)
                .phone("012345678")
                .passwordHash("hash")
                .role(UserRole.MEMBER)
                .status(UserStatus.ACTIVE)
                .fullNameKm("សមាជិក")
                .build();
    }

    private static RefreshToken activeToken(UUID value, User user) {
        return RefreshToken.builder()
                .id(20L)
                .user(user)
                .token(value)
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .build();
    }

    private static RefreshTokenRequest request(String value) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(value);
        return request;
    }

    private static void assertStatus(Runnable action, HttpStatus expectedStatus) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(
                        ((ResponseStatusException) error).getStatusCode().value()
                ).isEqualTo(expectedStatus.value()));
    }
}
