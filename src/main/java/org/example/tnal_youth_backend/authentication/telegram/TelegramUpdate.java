package org.example.tnal_youth_backend.authentication.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The small slice of Telegram's Bot API {@code Update} object this app
 * actually reads — a real update carries dozens more fields (edited
 * messages, callback queries, inline queries, etc.) that
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} lets us simply not
 * model.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {

    @JsonProperty("update_id")
    private Long updateId;

    private Message message;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private Chat chat;
        private String text;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chat {
        private Long id;
    }
}
