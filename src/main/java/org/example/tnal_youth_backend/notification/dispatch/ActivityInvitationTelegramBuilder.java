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
 * Renders the activity-invitation Telegram message as a bilingual letter —
 * a Khmer paragraph block, a blank line, then its English translation —
 * matching the wording and structure the org drafted by hand. Telegram
 * {@code HTML} parse mode only supports a small tag subset (bold/italic/
 * links), no layout, no custom fonts or colors.
 */
@Component
public class ActivityInvitationTelegramBuilder {

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

        boolean isPublic = Boolean.TRUE.equals(activity.getPublicActivity());
        String activityNameKm = escape(activity.getTitleKm());
        String activityNameEn = hasText(activity.getTitleEn()) ? escape(activity.getTitleEn()) : activityNameKm;
        String descriptionKm = hasText(activity.getDescription())
                ? escape(stripTrailingPunctuation(activity.getDescription()))
                : "";

        String venue = buildVenue(activity, false);
        String venueEn = buildVenue(activity, true);

        String dateKm = formatKhmerDate(startsAt);
        String dateEn = formatEnglishDate(startsAt);
        String timeKm = formatTimeRange(startsAt, endsAt);
        String timeEn = formatEnglishTimeRange(startsAt, endsAt);

        StringBuilder message = new StringBuilder();

        // Khmer
        message.append("ជម្រាបសួរ, ").append(escape(name)).append("\n\n");

        message.append("យើងខ្ញុំ សមាគមថ្នាលយុវជនកម្ពុជា");
        if (hasText(branchLabelKm)) {
            message.append(" ").append(escape(branchLabelKm));
        }
        message.append(" មានកិត្តិយសជាខ្លាំងក្នុងការរៀបចំសកម្មភាព \"")
                .append(activityNameKm)
                .append("\"");
        if (hasText(descriptionKm)) {
            message.append(" ដែលប្រព្រឹត្តទៅក្នុងគោលបំណង ").append(descriptionKm);
        }
        message.append("។\n\n");

        message.append("ដូចនេះដែរ យើងខ្ញុំសូមគោរពអញ្ជើញលោក លោកស្រី អ្នកនាងកញ្ញា ចូលរួមជាសមាជិកម្នាក់នៅក្នុងសកម្មភាពនេះរបស់សាខាយើងខ្ញុំ ដែលនឹងប្រព្រឹត្តទៅជា")
                .append(isPublic ? "សាធារណៈ" : "ឯកជន")
                .append("។\n\n");

        message.append("នាកាលបរិច្ឆេទ ").append(dateKm).append("\n\n");
        message.append("នៅវេលាម៉ោង ").append(timeKm).append("\n\n");
        message.append("ដែលស្ថិតនៅទីតាំង ").append(venue).append("\n\n");
        message.append("សូមចូលរួមដោយផ្ទាល់តាមថ្ងៃ និងម៉ោងដែលបានកំណត់។\n\n");
        message.append("ដោយក្តីគោរព\n");
        message.append(hasText(branchLabelKm) ? "ពី" + escape(branchLabelKm) : "TNAL Youth Cambodia");

        message.append("\n\n");

        // English
        message.append("Dear ").append(escape(name)).append(",\n\n");

        message.append("We, TNAL Youth Cambodia Association");
        if (hasText(branchNameEn)) {
            message.append(" – ").append(escape(branchNameEn)).append(" branch");
        }
        message.append(", are pleased to organize the activity \"")
                .append(activityNameEn)
                .append("\"");
        if (hasText(descriptionKm)) {
            message.append(", held for the purpose of ").append(descriptionKm);
        }
        message.append(".\n\n");

        message.append("We would therefore like to cordially invite you to join as a participant in this activity of our branch, which will be held as a ")
                .append(isPublic ? "public" : "private")
                .append(" session.\n\n");

        message.append("Date: ").append(dateEn).append("\n\n");
        message.append("Time: ").append(timeEn).append("\n\n");
        message.append("Venue: ").append(venueEn).append("\n\n");
        message.append("Please join directly at the scheduled date and time.\n\n");
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

    /**
     * Free-text descriptions often already end with their own "។" or ".",
     * and this template always adds its own closing punctuation right
     * after — stripping any trailing period first avoids a doubled "។។"
     * or "។." at the join point.
     */
    private String stripTrailingPunctuation(String value) {
        String trimmed = value.trim();

        while (trimmed.endsWith("។") || trimmed.endsWith(".")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        return trimmed;
    }

    /**
     * Escapes Telegram HTML parse-mode's three reserved characters. Unlike
     * a browser HTML escaper, quotes are intentionally left alone — this
     * text never sits inside an HTML attribute, only inside {@code <b>}/
     * {@code <i>} tag bodies, where Telegram doesn't require it.
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /** Same as {@link #escape}, plus quote-escaping for use inside {@code href="..."}. */
    private String escapeAttribute(String value) {
        return escape(value).replace("\"", "&quot;");
    }
}
