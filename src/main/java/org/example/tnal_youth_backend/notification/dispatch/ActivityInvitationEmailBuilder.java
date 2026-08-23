package org.example.tnal_youth_backend.notification.dispatch;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.member.branch.BranchLabels;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Renders the activity-invitation email as the same bilingual letter
 * {@link ActivityInvitationTelegramBuilder} sends to Telegram — a
 * personalized Khmer paragraph block addressed to the recipient by name,
 * then its English translation — wrapped in the org's card-style HTML/CSS
 * design. Used by {@link NotificationEmailSender} only for notifications
 * whose type code is "ACTIVITY_INVITATION"; every other notification type
 * keeps the plain-text email it already had.
 */
@Component
public class ActivityInvitationEmailBuilder {

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
        String titleKm = escape(activity.getTitleKm());

        String rawBranchNameKm = branch != null && hasText(branch.getNameKm()) ? branch.getNameKm() : "";
        String branchLabelKm = BranchLabels.withBranchPrefixKm(rawBranchNameKm);
        String branchNameEn = branch != null && hasText(branch.getNameEn()) ? branch.getNameEn() : rawBranchNameKm;

        boolean isPublic = Boolean.TRUE.equals(activity.getPublicActivity());
        String activityNameEn = hasText(activity.getTitleEn()) ? escape(activity.getTitleEn()) : titleKm;
        String descriptionKm = hasText(activity.getDescription())
                ? escape(stripTrailingPunctuation(activity.getDescription()))
                : "";

        String venueKm = buildVenue(activity, false);
        String venueEn = buildVenue(activity, true);

        String dateKm = formatKhmerDate(startsAt);
        String dateEn = formatEnglishDate(startsAt);
        String timeKm = formatTimeRange(startsAt, endsAt);
        String timeEn = formatEnglishTimeRange(startsAt, endsAt);

        String contact = branch != null && (hasText(branch.getPhone()) || hasText(branch.getEmail()))
                ? Stream.of(branch.getPhone(), branch.getEmail())
                        .filter(ActivityInvitationEmailBuilder::hasText)
                        .map(this::escape)
                        .collect(Collectors.joining(" &nbsp;&middot;&nbsp; "))
                : "";

        StringBuilder letterKm = new StringBuilder();
        letterKm.append("<p>ជម្រាបសួរ, ").append(escape(name)).append("</p>");
        letterKm.append("<p>យើងខ្ញុំ សមាគមថ្នាលយុវជនកម្ពុជា");
        if (hasText(branchLabelKm)) {
            letterKm.append(" ").append(escape(branchLabelKm));
        }
        letterKm.append(" មានកិត្តិយសជាខ្លាំងក្នុងការរៀបចំសកម្មភាព &laquo;").append(titleKm).append("&raquo;");
        if (hasText(descriptionKm)) {
            letterKm.append(" ដែលប្រព្រឹត្តទៅក្នុងគោលបំណង ").append(descriptionKm);
        }
        letterKm.append("។</p>");
        letterKm.append("<p>ដូចនេះដែរ យើងខ្ញុំសូមគោរពអញ្ជើញលោក លោកស្រី អ្នកនាងកញ្ញា ចូលរួមជាសមាជិកម្នាក់នៅក្នុងសកម្មភាពនេះរបស់សាខាយើងខ្ញុំ ដែលនឹងប្រព្រឹត្តទៅជា")
                .append(isPublic ? "សាធារណៈ" : "ឯកជន")
                .append("។</p>");
        letterKm.append("<p>នាកាលបរិច្ឆេទ ").append(dateKm).append("<br>")
                .append("នៅវេលាម៉ោង ").append(timeKm).append("<br>")
                .append("ដែលស្ថិតនៅទីតាំង ").append(venueKm).append("</p>");
        letterKm.append("<p>សូមចូលរួមដោយផ្ទាល់តាមថ្ងៃ និងម៉ោងដែលបានកំណត់។</p>");
        letterKm.append("<p>ដោយក្តីគោរព<br>")
                .append(hasText(branchLabelKm) ? "ពី" + escape(branchLabelKm) : "TNAL Youth Cambodia")
                .append("</p>");

        StringBuilder letterEn = new StringBuilder();
        letterEn.append("<p>Dear ").append(escape(name)).append(",</p>");
        letterEn.append("<p>We, TNAL Youth Cambodia Association");
        if (hasText(branchNameEn)) {
            letterEn.append(" &ndash; ").append(escape(branchNameEn)).append(" branch");
        }
        letterEn.append(", are pleased to organize the activity &laquo;").append(activityNameEn).append("&raquo;");
        if (hasText(descriptionKm)) {
            letterEn.append(", held for the purpose of ").append(descriptionKm);
        }
        letterEn.append(".</p>");
        letterEn.append("<p>We would therefore like to cordially invite you to join as a participant in this activity of our branch, which will be held as a ")
                .append(isPublic ? "public" : "private")
                .append(" session.</p>");
        letterEn.append("<p>Date: ").append(dateEn).append("<br>")
                .append("Time: ").append(timeEn).append("<br>")
                .append("Venue: ").append(venueEn).append("</p>");
        letterEn.append("<p>Please join directly at the scheduled date and time.</p>");
        letterEn.append("<p>Best regards,<br>")
                .append(hasText(branchNameEn) ? branchNameEn + " Branch" : "TNAL Youth Cambodia")
                .append("</p>");

        return TEMPLATE.formatted(
                titleKm,
                titleKm,
                activityNameEn,
                isPublic ? "សាធារណៈ &middot; Public Session" : "ឯកជន &middot; Private Session",
                letterKm.toString(),
                letterEn.toString(),
                hasText(contact) ? "<p class=\"contact\">" + contact + "</p>" : ""
        );
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
        return "%s &ndash; %s %s".formatted(toTime(start), toTime(end), khmerDayPart(start));
    }

    private String formatEnglishTimeRange(OffsetDateTime start, OffsetDateTime end) {
        return "%s &ndash; %s (+07)".formatted(toEnglishTime(start), toEnglishTime(end));
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

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeAttribute(String value) {
        return escape(value);
    }

    private static final String TEMPLATE = """
            <!doctype html>
            <html>
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <title>%s</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Kantumruy+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
            <style>
              :root{
                --bg:#F6F1EA; --surface:#FFFFFF; --surface-alt:#FBF7F1;
                --ink:#241C35; --muted:#746B87; --accent:#5C3D8C;
                --accent-2:#B08D3E; --line:#E4DCCB;
              }
              *{ box-sizing:border-box; }
              body{ margin:0; background:var(--bg); color:var(--ink); font-family:'Kantumruy Pro','Khmer UI',sans-serif; padding:48px 20px; text-align:center; }
              .page{ width:100%%; max-width:600px; margin:0 auto; text-align:center; }
              .eyebrow-row{ margin-bottom:22px; }
              .eyebrow{ font-size:11.5px; font-weight:600; letter-spacing:.22em; text-transform:uppercase; color:var(--accent-2); }
              .card{ background:var(--surface); border:1px solid var(--line); border-radius:4px; overflow:hidden; box-shadow:0 1px 3px rgba(36,28,53,.06), 0 18px 40px -24px rgba(36,28,53,.35); text-align:center; }
              .motif{ height:14px; width:100%%; background:var(--accent); font-size:0; line-height:0; }
              .head{ padding:44px 32px 30px; text-align:center; border-bottom:1px dashed var(--line); }
              .kicker{ font-size:12px; font-weight:500; letter-spacing:.18em; text-transform:uppercase; color:var(--muted); margin:0 0 18px; }
              .title-km{ font-weight:700; font-size:26px; line-height:1.5; color:var(--accent); margin:0 0 10px; }
              .title-en{ font-weight:500; font-style:italic; font-size:16px; color:var(--muted); margin:0; }
              .status-pill{ display:inline-block; margin-top:20px; padding:6px 14px; border:1px solid var(--accent-2); border-radius:100px; font-size:11.5px; font-weight:600; letter-spacing:.08em; text-transform:uppercase; color:var(--accent-2); }
              .letter{ padding:36px 40px; text-align:left; }
              .letter.km{ background:var(--surface); }
              .letter.en{ background:var(--surface-alt); border-top:1px dashed var(--line); }
              .letter p{ margin:0 0 16px; line-height:1.85; font-size:15px; color:var(--ink); }
              .letter.en p{ font-style:italic; color:var(--muted); font-size:13.5px; }
              .letter p:last-child{ margin-bottom:0; }
              .foot{ padding:20px 32px 36px; text-align:center; }
              .foot .contact{ font-size:12px; color:var(--muted); margin:0; }
              @media (max-width:520px){
                .head{ padding:36px 22px 26px; } .letter{ padding:28px 22px; }
                .foot{ padding:18px 22px 30px; } .title-km{ font-size:22px; }
              }
            </style>
            </head>
            <body>
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0"><tr><td align="center">
            <div class="page">
              <div class="eyebrow-row">
                <span class="eyebrow">សំបុត្រអញ្ជើញ &nbsp;&middot;&nbsp; Invitation</span>
              </div>
              <div class="card">
                <div class="motif" aria-hidden="true">&nbsp;</div>
                <div class="head">
                  <p class="kicker">សូមគោរពអញ្ជើញចូលរួម</p>
                  <h1 class="title-km">%s</h1>
                  <p class="title-en">%s</p>
                  <span class="status-pill">%s</span>
                </div>
                <div class="letter km">
                  %s
                </div>
                <div class="letter en">
                  %s
                </div>
                <div class="foot">
                  %s
                </div>
              </div>
            </div>
            </td></tr></table>
            </body>
            </html>
            """;
}
