package org.example.tnal_youth_backend.notification.dispatch;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.member.branch.BranchLabels;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Renders the "certificate ready" Telegram message as a bilingual letter,
 * same layout convention as {@link ActivityInvitationTelegramBuilder}.
 * {@code organizerBranch} is the activity's host branch (whose staff
 * prepared the certificates), named explicitly so a recipient staffing more
 * than one branch knows which activity's certificates this refers to.
 */
@Component
public class CertificateReadyTelegramBuilder {

    private static final ZoneOffset CAMBODIA_OFFSET = ZoneOffset.of("+07:00");

    private static final String[] KHMER_WEEKDAYS = {
            "ថ្ងៃអាទិត្យ", "ថ្ងៃចន្ទ", "ថ្ងៃអង្គារ", "ថ្ងៃពុធ",
            "ថ្ងៃព្រហស្បតិ៍", "ថ្ងៃសុក្រ", "ថ្ងៃសៅរ៍"
    };

    private static final String[] KHMER_MONTHS = {
            "មករា", "កុម្ភៈ", "មីនា", "មេសា", "ឧសភា", "មិថុនា",
            "កក្កដា", "សីហា", "កញ្ញា", "តុលា", "វិច្ឆិកា", "ធ្នូ"
    };

    public String build(
            Activity activity,
            Branch organizerBranch,
            String recipientNameKm
    ) {
        String name = hasText(recipientNameKm) ? recipientNameKm : "សមាជិក";

        String rawOrganizerNameKm = organizerBranch != null && hasText(organizerBranch.getNameKm())
                ? organizerBranch.getNameKm() : "";
        String organizerLabelKm = BranchLabels.withBranchPrefixKm(rawOrganizerNameKm);
        String organizerNameEn = organizerBranch != null && hasText(organizerBranch.getNameEn())
                ? organizerBranch.getNameEn() : rawOrganizerNameKm;
        String organizerLabelEn = BranchLabels.withBranchPrefixEn(organizerNameEn);

        String activityNameKm = escape(activity.getTitleKm());
        String activityNameEn = hasText(activity.getTitleEn()) ? escape(activity.getTitleEn()) : activityNameKm;

        String dateKm = activity.getStartsAt() == null ? "" : formatKhmerDate(activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET));
        String dateEn = activity.getStartsAt() == null ? "" : formatEnglishDate(activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET));

        StringBuilder message = new StringBuilder();

        // Khmer
        message.append("ជម្រាបសួរ ").append(escape(name)).append("\n\n");

        message.append("អបអរសាទរ! អ្នកទទួលបានវិញ្ញាបនបត្របញ្ជាក់អំពីការចូលរួមរបស់អ្នកក្នុងកម្មវិធី \"")
                .append(activityNameKm)
                .append("\"");

        if (hasText(dateKm)) {
            message.append(" ដែលបានប្រព្រឹត្តទៅនៅថ្ងៃទី ").append(dateKm);
        }

        message.append(" របស់");
        message.append(hasText(organizerLabelKm) ? escape(organizerLabelKm) : "សាខាយើងខ្ញុំ");
        message.append("។\n\n");

        message.append("អ្នកអាចចូលទៅទាញយកឯកសារនៅក្នុងគេហទំព័រយុវជនបាន។\n\n");
        message.append("សូមគោរពអរគុណយ៉ាងក្រៃលែងចំពោះវត្តមានដ៏ខ្ពង់ខ្ពស់របស់អ្នកនៅក្នុងកម្មវិធី។\n\n");
        message.append("ពី");
        message.append(hasText(organizerLabelKm) ? escape(organizerLabelKm) : "TNAL Youth Cambodia");

        message.append("\n\n");

        // English
        message.append("Dear ").append(escape(name)).append(",\n\n");

        message.append("Congratulations! You have received a certificate confirming your participation in the activity \"")
                .append(activityNameEn)
                .append("\"");

        if (hasText(dateEn)) {
            message.append(", held on ").append(dateEn);
        }

        message.append(", from ");
        message.append(hasText(organizerLabelEn) ? organizerLabelEn : "our branch");
        message.append(".\n\n");

        message.append("You may log in to the youth website to download the document.\n\n");
        message.append("We sincerely thank you for your valued presence at the activity.\n\n");
        message.append("From ");
        message.append(hasText(organizerLabelEn) ? organizerLabelEn : "TNAL Youth Cambodia");

        return message.toString();
    }

    private String formatKhmerDate(OffsetDateTime date) {
        String weekday = KHMER_WEEKDAYS[date.getDayOfWeek().getValue() % 7];
        return "%s ទី%d ខែ%s ឆ្នាំ%d".formatted(
                weekday,
                date.getDayOfMonth(),
                KHMER_MONTHS[date.getMonthValue() - 1],
                date.getYear()
        );
    }

    private String formatEnglishDate(OffsetDateTime date) {
        String weekday = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
        String month = date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
        return "%s, %s %d, %d".formatted(weekday, month, date.getDayOfMonth(), date.getYear());
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
