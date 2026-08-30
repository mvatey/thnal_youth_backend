package org.example.tnal_youth_backend.notification.dispatch;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.member.branch.BranchLabels;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Renders the "activity tomorrow" reminder Telegram message as a bilingual
 * letter, same layout convention as {@link ActivityInvitationTelegramBuilder}.
 */
@Component
public class ActivityReminderTelegramBuilder {

    private static final ZoneOffset CAMBODIA_OFFSET = ZoneOffset.of("+07:00");

    public String build(
            Activity activity,
            Branch branch,
            String recipientNameKm
    ) {
        String name = hasText(recipientNameKm) ? recipientNameKm : "សមាជិក";

        String rawBranchNameKm = branch != null && hasText(branch.getNameKm()) ? branch.getNameKm() : "";
        String branchLabelKm = BranchLabels.withBranchPrefixKm(rawBranchNameKm);
        String branchNameEn = branch != null && hasText(branch.getNameEn()) ? branch.getNameEn() : rawBranchNameKm;

        String activityNameKm = escape(activity.getTitleKm());
        String activityNameEn = hasText(activity.getTitleEn()) ? escape(activity.getTitleEn()) : activityNameKm;

        String venue = buildVenue(activity);
        String timeKm = activity.getStartsAt() == null || activity.getEndsAt() == null
                ? "" : formatTimeRange(
                        activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET),
                        activity.getEndsAt().withOffsetSameInstant(CAMBODIA_OFFSET));
        String timeEn = activity.getStartsAt() == null || activity.getEndsAt() == null
                ? "" : formatEnglishTimeRange(
                        activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET),
                        activity.getEndsAt().withOffsetSameInstant(CAMBODIA_OFFSET));

        StringBuilder message = new StringBuilder();

        // Khmer
        message.append("ជម្រាបសួរ ").append(escape(name)).append("\n\n");

        message.append("យើងខ្ញុំសូមរំលឹកជូនដំណឹងមកទៅលោកអំពីកម្មវិធី \"")
                .append(activityNameKm)
                .append("\" ដែលនឹងប្រព្រឹត្តទៅនាថ្ងៃស្អែក");

        if (hasText(timeKm)) {
            message.append(" ម៉ោង ").append(timeKm);
        }

        message.append(" ទីតាំង ").append(venue).append("។\n\n");

        message.append("សូមគោរពចូលរួមតាមពេលកំណត់។ សូមអរគុណ។\n\n");
        message.append("ពី");
        message.append(hasText(branchLabelKm) ? escape(branchLabelKm) : "TNAL Youth Cambodia");

        message.append("\n\n");

        // English
        message.append("Dear ").append(escape(name)).append(",\n\n");

        message.append("We would like to remind you about the activity \"")
                .append(activityNameEn)
                .append("\", which will take place tomorrow");

        if (hasText(timeEn)) {
            message.append(" at ").append(timeEn);
        }

        message.append(", at ").append(venue).append(".\n\n");

        message.append("Please join us at the scheduled time. Thank you.\n\n");
        message.append("From ");
        message.append(hasText(branchNameEn) ? branchNameEn + " Branch" : "TNAL Youth Cambodia");

        return message.toString();
    }

    private String buildVenue(Activity activity) {
        String locationName = hasText(activity.getLocationName())
                ? activity.getLocationName()
                : hasText(activity.getAddress())
                        ? activity.getAddress()
                        : "TBA";

        return escape(locationName);
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
}
