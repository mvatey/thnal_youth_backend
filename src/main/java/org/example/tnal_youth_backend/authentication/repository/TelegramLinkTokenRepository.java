package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.TelegramLinkToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface TelegramLinkTokenRepository
        extends JpaRepository<TelegramLinkToken, Long> {

    Optional<TelegramLinkToken> findByToken(
            String token
    );

    /**
     * The most recent still-usable (unconsumed, unexpired) token for this
     * user, if any — reused by {@code TelegramLinkService#getConnectInfo}
     * instead of minting a fresh token on every page load.
     */
    Optional<TelegramLinkToken>
    findTopByUser_IdAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            OffsetDateTime now
    );
}
