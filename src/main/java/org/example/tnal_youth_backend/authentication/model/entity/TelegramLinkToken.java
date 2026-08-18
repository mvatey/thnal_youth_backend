package org.example.tnal_youth_backend.authentication.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * A one-time-ish "connect your Telegram" link handed to a member via the
 * activity page's reminder banner (see
 * {@code authentication.telegram.TelegramLinkService}). The bot's own
 * server calls back into {@code POST /api/telegram/link} with this token
 * plus the chat id it saw on {@code /start <token>}, which consumes the
 * token and links {@link User#getTelegramChatId()}.
 */
@Entity
@Table(name = "telegram_link_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramLinkToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
