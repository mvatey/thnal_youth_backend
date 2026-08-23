package org.example.tnal_youth_backend.authentication.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.notification.dispatch.TelegramMessageSender;
import org.springframework.stereotype.Service;

/**
 * Handles an inbound Telegram {@code Update} — today that only ever means
 * one thing: a user tapped the app's "connect Telegram" deep link, which
 * opens a chat with the bot and auto-sends {@code /start <token>}. This is
 * the piece that was missing before: something has to actually read that
 * message and call {@link TelegramLinkService#confirmLink}. Reached from
 * either {@link TelegramWebhookController} (production, needs a public
 * HTTPS URL registered with Telegram) or {@link TelegramPollingScheduler}
 * (works anywhere, including local dev, no public URL needed).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramUpdateHandler {

    private final TelegramLinkService telegramLinkService;
    private final TelegramMessageSender telegramMessageSender;

    public void handle(TelegramUpdate update) {
        if (update == null || update.getMessage() == null) {
            return;
        }

        TelegramUpdate.Message message = update.getMessage();

        if (message.getChat() == null || message.getChat().getId() == null) {
            return;
        }

        Long chatId = message.getChat().getId();
        String text = message.getText();

        if (text == null || !text.startsWith("/start")) {
            // Anything other than /start is out of scope for this bot
            // today — silently ignored rather than replied to.
            return;
        }

        handleStart(chatId, text);
    }

    private void handleStart(Long chatId, String text) {
        String token = text.length() > "/start".length()
                ? text.substring("/start".length()).trim()
                : "";

        if (token.isEmpty()) {
            reply(
                    chatId,
                    "សូមប្រើតំណភ្ជាប់ពីកម្មវិធីដើម្បីភ្ជាប់គណនីរបស់អ្នក។\n"
                            + "Please use the connect link from the app to link your account."
            );
            return;
        }

        try {
            telegramLinkService.confirmLink(token, chatId);

            reply(
                    chatId,
                    "សូមស្វាគមន៍មកកាន់ @TnalYouth_Bot!\n\n"
                            + "ការភ្ជាប់គណនីរបស់អ្នកបានជោគជ័យ! អ្នកនឹងទទួលបានការជូនដំណឹងភ្លាមៗអំពីសកម្មភាព និងឯកសារផ្សេងៗនៅទីនេះ។\n\n"
                            + "Welcome to @TnalYouth_Bot!\n\n"
                            + "You have successfully connected your account! You will now receive real-time notifications of activities and documents here."
            );
        } catch (BusinessException e) {
            log.info("Telegram link attempt failed for chat {}: {}", chatId, e.getMessage());

            reply(
                    chatId,
                    "⚠️ តំណភ្ជាប់នេះមិនត្រឹមត្រូវ ឬផុតកំណត់ហើយ។ សូមព្យាយាមម្តងទៀតពីកម្មវិធី។\n"
                            + "⚠️ This link is invalid or expired. Please try again from the app."
            );
        }
    }

    private void reply(Long chatId, String text) {
        try {
            telegramMessageSender.sendRaw(chatId, text, null);
        } catch (Exception e) {
            // The account link itself already succeeded/failed and was
            // handled above — a failure to send the confirmation text
            // is just logged, not allowed to look like the link failed.
            log.warn("TelegramUpdateHandler: failed to reply to chat {}", chatId, e);
        }
    }
}
