package org.example.tnal_youth_backend.authentication.telegram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response for {@code GET /api/telegram/connect-info}. When
 * {@code connected} is {@code true}, {@code deepLink} is {@code null} — the
 * activity page's yellow reminder banner is expected to hide itself in that
 * case rather than render a stale link.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramConnectInfoResponse {
    private boolean connected;
    private String deepLink;
}
