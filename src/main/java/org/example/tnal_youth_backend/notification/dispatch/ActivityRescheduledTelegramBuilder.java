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
 * Renders the "activity date changed" Telegram message as a bilingual
 * letter -- a Khmer paragraph block, a blank line, then its English
 * translation -- matching the wording and structure of the org's hand-drafted
 * template. Mirrors {@link ActivityInvitationTelegramBuilder}'s layout and
 * date-formatting conventions.
 */
@Component
public class ActivityRescheduledTelegramBuilder {

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
        OffsetDateTime endsAt = activity.getEndsAt().withOffsetSameInstant(CAMBODIA_OFFSET);

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
        String timeKm = formatTimeRange(startsAt, endsAt);
        String timeEn = formatEnglishTimeRange(startsAt, endsAt);

        StringBuilder message = new StringBuilder();

        // Khmer
        message.append("<b>កាលបរិច្ឆេទកម្មវិធីត្រូវបានផ្លាស់ប្តូរ</b>\n\n");
        message.append("ជម្រាបសួរ ").append(escape(name)).append("\n\n");

        message.append("យើងខ្ញុំសូមជម្រាបដំណឹងថា កាលបរិច្ឆេទរបស់កម្មវិធី \"")
                .append(activityNameKm)
                .append("\" ត្រូវបានផ្លាស់ប្តូរទៅជាកាលបរិច្ឆេទថ្មី\n\n");

        message.append("នាថ្ងៃទី ").append(dateKm).append("\n\n");
        message.append("ម៉ោង ").append(timeKm).append("\n\n");
        message.append("ទីតាំង ").append(venue).append("\n\n");

        message.append("សូមអធ្យាស្រ័យដល់ការជូនដំណឹងភ្លាមៗ និងសូមលោក លោកស្រី អ្នកនាងកញ្ញា មកចូលរួមតាមពេលកំណត់ជាថ្មីម្តងទៀត។\n\n");
        message.append("ដោយក្ដីយោគយល់\n");
        message.append(hasText(branchLabelKm) ? escape(branchLabelKm) : "TNAL Youth Cambodia");

        message.append("\n\n");

        // English
        message.append("<b>Activity Date Changed</b>\n\n");
        message.append("Dear ").append(escape(name)).append(",\n\n");

        message.append("We would like to inform you that the schedule of the activity \"")
                .append(activityNameEn)
                .append("\" has been changed to a new date.\n\n");

        message.append("Date: ").append(dateEn).append("\n\n");
        message.append("Time: ").append(timeEn).append("\n\n");
        message.append("Venue: ").append(venueEn).append("\n\n");

        message.append("We apologize for the short notice and kindly ask you to join according to the newly scheduled time.\n\n");
        message.append("Best regards,\n");
        message.append(hasText(branchNameEn) ? branchNameEn + " Branch" : "TNAL Youth Cambodia");

        return message.toString();
    }

    private String buildVenue(Activity activity, boolean english) {
        /*
         * locationName is a distinct, optional field from address that the
         * activity-creation form never actually exposes an input for, so it
         * is blank for effectively every activity -- falling back to
         * address here matches the same locationName-or-address fallback
         * already used to display the venue on the activity list/detail
         * pages (see activity/[id]/page.js), instead of showing "TBA" for
         * an activity that does have a venue.
         */
        String locationName = hasText(activity.getLocationName())
                ? activity.getLocationName()
                : hasText(activity.getAddress())
                        ? activity.getAddress()
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

    private String formatTimeRange(OffsetDateTime start, OffsetDateTime end) {
        return "%s – %s %s".formatted(toTime(start), toTime(end), khmerDayPart(start));
    }

    private String formatEnglishTimeRange(OffsetDateTime start, OffsetDateTime end) {
        return "%s – %s (+07)".formatted(toEnglishTime(start), toEnglishTime(end));
    }

    private String toTime(OffsetDateTime time) {
        return "%d:%02d".formatted(time.getHour(), time.getMinute());
    }

    private String toEnglishTime(OffsetDateTime time) {
        int hour12 = time.getHour() % 12 == 0 ? 12 : time.getHour() % 12;
        return "%d:%02d %s".formatted(hour12, time.getMinute(), time.getHour() < 12 ? "AM" : "PM");
    }

    private String khmerDayPart(OffsetDateTime time) {
        return time.getHour() < 12 ? "ព្រឹក" : "ល្ងាច";
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
