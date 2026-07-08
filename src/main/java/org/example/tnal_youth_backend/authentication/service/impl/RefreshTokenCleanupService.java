package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredOrRevoked(OffsetDateTime.now());
        log.info("Expired/revoked refresh tokens cleaned up at {}", OffsetDateTime.now());
    }
}