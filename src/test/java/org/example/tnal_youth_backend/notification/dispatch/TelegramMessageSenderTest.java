package org.example.tnal_youth_backend.notification.dispatch;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.notification.model.NotificationModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramMessageSenderTest {

    @Test
    void missingBotTokenFailsInsteadOfReportingFalseSuccess() {
        TelegramMessageSender sender = new TelegramMessageSender(
                new RestTemplateBuilder()
        );
        ReflectionTestUtils.setField(sender, "botToken", "   ");

        User user = User.builder()
                .telegramChatId(123456789L)
                .build();
        NotificationModel notification = NotificationModel.builder()
                .id(1L)
                .title("Test notification")
                .build();

        assertThatThrownBy(() -> sender.send(user, notification))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
    }
}
