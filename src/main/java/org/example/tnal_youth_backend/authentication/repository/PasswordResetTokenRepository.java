package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken>
    findTopByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndCreatedAtAfter(
            Long userId,
            OffsetDateTime createdAfter
    );

    Optional<PasswordResetToken>
    findTopByUser_IdAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            OffsetDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE PasswordResetToken token
        SET token.consumedAt = :consumedAt
        WHERE token.user.id = :userId
          AND token.consumedAt IS NULL
        """)
    int invalidateAllUnconsumedTokensForUser(
            @Param("userId") Long userId,
            @Param("consumedAt") OffsetDateTime consumedAt
    );
}