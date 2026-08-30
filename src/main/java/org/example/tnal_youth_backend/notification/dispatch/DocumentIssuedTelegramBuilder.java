package org.example.tnal_youth_backend.notification.dispatch;

import org.example.tnal_youth_backend.document.document.entity.Document;
import org.springframework.stereotype.Component;

/**
 * Renders the "document issued" Telegram message as a bilingual letter,
 * same layout convention as {@link ActivityInvitationTelegramBuilder}. Used
 * for every document a member receives (membership certificate, letters,
 * etc.) — the document's own {@link Document#getTitle()} is the only
 * per-send variable.
 */
@Component
public class DocumentIssuedTelegramBuilder {

    public String build(Document document, String recipientNameKm) {
        String name = hasText(recipientNameKm) ? recipientNameKm : "សមាជិក";
        String title = escape(document.getTitle());

        StringBuilder message = new StringBuilder();

        // Khmer
        message.append("ជម្រាបសួរ ").append(escape(name)).append("\n\n");
        message.append("សូមស្វាគមន៍ក្នុងការចូលរួមជាមួយសមាគមថ្នាលយុវជនកម្ពុជា។\n\n");
        message.append("អ្នកទទួលបាន \"").append(title).append("\" ដែលបញ្ជាក់អំពីការចូលរួមរបស់អ្នក។ ");
        message.append("សូមចូលទៅកាន់គណនីរបស់អ្នកដើម្បីមើលឯកសារ។\n\n");
        message.append("ពីថ្នាលយុវជនកម្ពុជា");

        message.append("\n\n");

        // English
        message.append("Dear ").append(escape(name)).append(",\n\n");
        message.append("Welcome to the Cambodian Youth Nursery Association (TNAL Youth).\n\n");
        message.append("You have received \"").append(title).append("\", confirming your participation. ");
        message.append("Please log in to your account to view the document.\n\n");
        message.append("From TNAL Youth Cambodia");

        return message.toString();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
