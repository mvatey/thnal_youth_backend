package org.example.tnal_youth_backend.notification.dispatch;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.member.branch.BranchLabels;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Renders the "activity cancelled" Telegram message as a bilingual letter,
 * same layout convention as {@link ActivityInvitationTelegramBuilder} and
 * {@link ActivityRescheduledTelegramBuilder}. Includes the staff-entered
 * cancellation reason ({@link Activity#getCancellationReason()}) when one
 * was given, and drops that clause entirely when it wasn't rather than
 * showing an empty "due to ." tail.
 */
@Component
public class ActivityCancelledTelegramBuilder {

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
            Branch branch,
            String recipientNameKm
    ) {
        OffsetDateTime startsAt = activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET);

        String name = hasText(recipientNameKm) ? recipientNameKm : "សមាជិក";

        String rawBranchNameKm = branch != null && hasText(branch.getNameKm()) ? branch.getNameKm() : "";
        String branchLabelKm = BranchLabels.withBranchPrefixKm(rawBranchNameKm);
        String branchNameEn = branch != null && hasText(branch.getNameEn()) ? branch.getNameEn() : rawBranchNameKm;

        String activityNameKm = escape(activity.getTitleKm());
        String activityNameEn = hasText(activity.getTitleEn()) ? escape(activity.getTitleEn()) : activityNameKm;

        String venue = buildVenue(activity, false);
        String venueEn = buildVenue(activity, true);

        String dateKm = formatKhmerDate(startsAt);
        String dateEn = formatEnglishDate(startsAt);

        String reasonKm = hasText(activity.getCancellationReason())
                ? escape(activity.getCancellationReason().trim())
                : null;

        StringBuilder message = new StringBuilder();

        // Khmer
        message.append("<b>កម្មវិធីត្រូវបានលុបចោល</b>\n\n");
        message.append("ជម្រាបសួរ ").append(escape(name)).append("\n\n");

        message.append("យើងខ្ញុំសូមជម្រាបដំណឹងថា កម្មវិធី \"")
                .append(activityNameKm)
                .append("\" ដែលនឹងប្រព្រឹត្តទៅនៅថ្ងៃទី ")
                .append(dateKm)
                .append(" ទីតាំង ")
                .append(venue)
                .append(" ត្រូវបានលុបចោល");

        if (reasonKm != null) {
            message.append(" ដោយមូលហេតុ ").append(reasonKm);
        }
        message.append("។\n\n");

        message.append("សូមអធ្យាស្រ័យដល់ការជូនដំណឹងភ្លាមៗ។\n\n");
        message.append("ដោយក្ដីយោគយល់\n");
        message.append(hasText(branchLabelKm) ? escape(branchLabelKm) : "TNAL Youth Cambodia");

        message.append("\n\n");

        // English
        message.append("<b>Activity Cancelled</b>\n\n");
        message.append("Dear ").append(escape(name)).append(",\n\n");

        message.append("We would like to inform you that the activity \"")
                .append(activityNameEn)
                .append("\", originally scheduled on ")
                .append(dateEn)
                .append(" at ")
                .append(venueEn)
                .append(", has been cancelled");

        if (reasonKm != null) {
            message.append(" due to ").append(reasonKm);
        }
        message.append(".\n\n");

        message.append("We apologize for the short notice.\n\n");
        message.append("Best regards,\n");
        message.append(hasText(branchNameEn) ? branchNameEn + " Branch" : "TNAL Youth Cambodia");

        return message.toString();
    }

    private String buildVenue(Activity activity, boolean english) {
        String locationName = hasText(activity.getLocationName())
                ? activity.getLocationName()
                : (english ? "TBA" : "មិនទាន់កំណត់");

        if (hasText(activity.getGoogleMapUrl())) {
            return "<a href=\"" + escapeAttribute(activity.getGoogleMapUrl()) + "\">" + escape(locationName) + "</a>";
        }

        return escape(locationName);
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
        String weekday = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
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

    private String escapeAttribute(String value) {
        return escape(value).replace("\"", "&quot;");
    }
}
