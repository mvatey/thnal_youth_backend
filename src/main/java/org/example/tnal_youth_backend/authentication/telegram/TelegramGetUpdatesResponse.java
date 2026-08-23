package org.example.tnal_youth_backend.authentication.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Response envelope for {@code GET .../getUpdates}. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramGetUpdatesResponse {
    private boolean ok;
    private List<TelegramUpdate> result;
}
