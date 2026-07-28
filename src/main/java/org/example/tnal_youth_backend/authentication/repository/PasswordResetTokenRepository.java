package org.example.tnal_youth_backend.authentication.repository;

import org.example.tnal_youth_backend.authentication.model.entity.PasswordResetToken;
import org.example.tnal_youth_backend.authentication.model.enums.OtpPurpose;
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
    findTopByUser_IdAndPurposeOrderByCreatedAtDesc(
            Long userId,
            OtpPurpose purpose
    );

    long countByUser_IdAndPurposeAndCreatedAtAfter(
            Long userId,
            OtpPurpose purpose,
            OffsetDateTime createdAfter
    );

    Optional<PasswordResetToken>
    findTopByUser_IdAndPurposeAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            OtpPurpose purpose,
            OffsetDateTime now
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE PasswordResetToken token
            SET token.consumedAt = :consumedAt
            WHERE token.user.id = :userId
              AND token.purpose = :purpose
              AND token.consumedAt IS NULL
            """)
    int invalidateAllUnconsumedTokensForUserAndPurpose(
            @Param("userId")
            Long userId,

            @Param("purpose")
            OtpPurpose purpose,

            @Param("consumedAt")
            OffsetDateTime consumedAt
    );
}