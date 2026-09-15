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
 * Renders the branch co-hosting invitation Telegram message as a bilingual
 * letter — matching {@link ActivityInvitationTelegramBuilder}'s wording and
 * structure for an individual member invitation, but addressed to a
 * BRANCH's staff (invited to co-host, not to attend as a participant) and
 * closing with a call to respond (accept/decline) instead of just "please
 * attend." Telegram {@code HTML} parse mode only supports a small tag
 * subset (bold/italic/links), no layout, no custom fonts or colors.
 */
@Component
public class ActivityBranchInvitationTelegramBuilder {

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
            Branch invitedBranch,
            String recipientNameKm
    ) {
        OffsetDateTime startsAt = activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET);
        OffsetDateTime endsAt = activity.getEndsAt().withOffsetSameInstant(CAMBODIA_OFFSET);

        String name = hasText(recipientNameKm) ? recipientNameKm : "សមាជិក";

        String rawOrganizerNameKm = organizerBranch != null && hasText(organizerBranch.getNameKm())
                ? organizerBranch.getNameKm() : "-";
        String organizerLabelKm = BranchLabels.withBranchPrefixKm(rawOrganizerNameKm);
        String organizerNameEn = organizerBranch != null && hasText(organizerBranch.getNameEn())
                ? organizerBranch.getNameEn() : rawOrganizerNameKm;
        String organizerLabelEn = BranchLabels.withBranchPrefixEn(organizerNameEn);

        String rawInvitedNameKm = invitedBranch != null && hasText(invitedBranch.getNameKm())
                ? invitedBranch.getNameKm() : "";
        String invitedLabelKm = BranchLabels.withBranchPrefixKm(rawInvitedNameKm);
        String invitedNameEn = invitedBranch != null && hasText(invitedBranch.getNameEn())
                ? invitedBranch.getNameEn() : rawInvitedNameKm;
        String invitedLabelEn = BranchLabels.withBranchPrefixEn(invitedNameEn);

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

        message.append("យើងខ្ញុំ សមាគមថ្នាលយុវជនកម្ពុជា ").append(escape(organizerLabelKm));
        message.append(" មានកិត្តិយសជាខ្លាំងក្នុងការរៀបចំសកម្មភាព \"")
                .append(activityNameKm)
                .append("\"");
        if (hasText(descriptionKm)) {
            message.append(" ដែលប្រព្រឹត្តទៅក្នុងគោលបំណង ").append(descriptionKm);
        }
        message.append("។\n\n");

        message.append("ដូចនេះដែរ យើងខ្ញុំសូមគោរពអញ្ជើញ")
                .append(invitedLabelKm.isBlank() ? "សាខារបស់លោកអ្នក" : escape(invitedLabelKm))
                .append(" ចូលរួមសហការរៀបចំសកម្មភាពនេះជាមួយយើងខ្ញុំ ក្នុងនាមជាសាខាសហការរៀបចំ ដែលនឹងប្រព្រឹត្តទៅជា")
                .append(isPublic ? "សាធារណៈ" : "ឯកជន")
                .append("។\n\n");

        message.append("នាកាលបរិច្ឆេទ ").append(dateKm).append("\n\n");
        message.append("នៅវេលាម៉ោង ").append(timeKm).append("\n\n");
        message.append("ដែលស្ថិតនៅទីតាំង ").append(venue).append("\n\n");
        message.append("សូមចូលទៅកាន់ប្រព័ន្ធគ្រប់គ្រង ដើម្បីពិនិត្យលម្អិត និងឆ្លើយតប (ព្រមទទួល ឬបដិសេធ) ចំពោះការអញ្ជើញនេះ។\n\n");
        message.append("ដោយក្តីគោរព\n");
        message.append("ពី").append(escape(organizerLabelKm));

        message.append("\n\n");

        // English
        message.append("Dear ").append(escape(name)).append(",\n\n");

        message.append("We, TNAL Youth Cambodia Association – ").append(escape(organizerNameEn)).append(" branch");
        message.append(", are pleased to organize the activity \"")
                .append(activityNameEn)
                .append("\"");
        if (hasText(descriptionKm)) {
            message.append(", held for the purpose of ").append(descriptionKm);
        }
        message.append(".\n\n");

        message.append("We would therefore like to cordially invite ")
                .append(invitedLabelEn.isBlank() ? "your branch" : escape(invitedLabelEn))
                .append(" to co-organize this activity together with us as a co-hosting branch, which will be held as a ")
                .append(isPublic ? "public" : "private")
                .append(" session.\n\n");

        message.append("Date: ").append(dateEn).append("\n\n");
        message.append("Time: ").append(timeEn).append("\n\n");
        message.append("Venue: ").append(venueEn).append("\n\n");
        message.append("Please visit the management system to review the details and respond (accept or decline) to this invitation.\n\n");
        message.append("Best regards,\n");
        message.append(escape(organizerLabelEn));

        return message.toString();
    }

    private String buildVenue(Activity activity, boolean english) {
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

    private String stripTrailingPunctuation(String value) {
        String trimmed = value.trim();

        while (trimmed.endsWith("។") || trimmed.endsWith(".")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        return trimmed;
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
