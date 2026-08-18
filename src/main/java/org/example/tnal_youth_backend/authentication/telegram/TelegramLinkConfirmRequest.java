package org.example.tnal_youth_backend.authentication.telegram;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for {@code POST /api/telegram/link}, sent by the bot's own server
 * (not the browser) after it sees {@code /start <token>} from a member.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TelegramLinkConfirmRequest {

    @NotBlank
    private String token;

    @NotNull
    private Long chatId;
}
