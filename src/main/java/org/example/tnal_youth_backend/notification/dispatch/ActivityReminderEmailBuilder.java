package org.example.tnal_youth_backend.notification.dispatch;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.member.branch.BranchLabels;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Renders the "activity tomorrow" reminder email as the same bilingual
 * letter {@link ActivityReminderTelegramBuilder} sends to Telegram, wrapped
 * in the org's card-style HTML/CSS design shared with
 * {@link ActivityInvitationEmailBuilder}.
 */
@Component
public class ActivityReminderEmailBuilder {

    private static final ZoneOffset CAMBODIA_OFFSET = ZoneOffset.of("+07:00");

    public String build(
            Activity activity,
            Branch branch,
            String recipientNameKm
    ) {
        String name = hasText(recipientNameKm) ? recipientNameKm : "សមាជិក";
        String titleKm = escape(activity.getTitleKm());
        String activityNameEn = hasText(activity.getTitleEn()) ? escape(activity.getTitleEn()) : titleKm;

        String rawBranchNameKm = branch != null && hasText(branch.getNameKm()) ? branch.getNameKm() : "";
        String branchLabelKm = BranchLabels.withBranchPrefixKm(rawBranchNameKm);
        String branchNameEn = branch != null && hasText(branch.getNameEn()) ? branch.getNameEn() : rawBranchNameKm;

        String venue = buildVenue(activity);
        String timeKm = activity.getStartsAt() == null || activity.getEndsAt() == null
                ? "" : formatTimeRange(
                        activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET),
                        activity.getEndsAt().withOffsetSameInstant(CAMBODIA_OFFSET));
        String timeEn = activity.getStartsAt() == null || activity.getEndsAt() == null
                ? "" : formatEnglishTimeRange(
                        activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET),
                        activity.getEndsAt().withOffsetSameInstant(CAMBODIA_OFFSET));

        String contact = branch != null && (hasText(branch.getPhone()) || hasText(branch.getEmail()))
                ? Stream.of(branch.getPhone(), branch.getEmail())
                        .filter(ActivityReminderEmailBuilder::hasText)
                        .map(this::escape)
                        .collect(Collectors.joining(" &nbsp;&middot;&nbsp; "))
                : "";

        StringBuilder letterKm = new StringBuilder();
        letterKm.append("<p>ជម្រាបសួរ ").append(escape(name)).append("</p>");
        letterKm.append("<p>យើងខ្ញុំសូមរំលឹកជូនដំណឹងមកទៅលោកអំពីកម្មវិធី &laquo;").append(titleKm).append("&raquo; ដែលនឹងប្រព្រឹត្តទៅនាថ្ងៃស្អែក");
        if (hasText(timeKm)) {
            letterKm.append(" ម៉ោង ").append(timeKm);
        }
        letterKm.append(" ទីតាំង ").append(venue).append("។</p>");
        letterKm.append("<p>សូមគោរពចូលរួមតាមពេលកំណត់។ សូមអរគុណ។</p>");
        letterKm.append("<p>ពី").append(hasText(branchLabelKm) ? escape(branchLabelKm) : "TNAL Youth Cambodia").append("</p>");

        StringBuilder letterEn = new StringBuilder();
        letterEn.append("<p>Dear ").append(escape(name)).append(",</p>");
        letterEn.append("<p>We would like to remind you about the activity &laquo;").append(activityNameEn).append("&raquo;, which will take place tomorrow");
        if (hasText(timeEn)) {
            letterEn.append(" at ").append(timeEn);
        }
        letterEn.append(", at ").append(venue).append(".</p>");
        letterEn.append("<p>Please join us at the scheduled time. Thank you.</p>");
        letterEn.append("<p>From ").append(hasText(branchNameEn) ? branchNameEn + " Branch" : "TNAL Youth Cambodia").append("</p>");

        return TEMPLATE.formatted(
                titleKm,
                titleKm,
                activityNameEn,
                "ថ្ងៃស្អែក &middot; Tomorrow",
                letterKm.toString(),
                letterEn.toString(),
                hasText(contact) ? "<p class=\"contact\">" + contact + "</p>" : ""
        );
    }

    private String buildVenue(Activity activity) {
        String locationName = hasText(activity.getLocationName())
                ? activity.getLocationName()
                : hasText(activity.getAddress())
                        ? activity.getAddress()
                        : "TBA";

        if (hasText(activity.getGoogleMapUrl())) {
            return "<a href=\"" + escape(activity.getGoogleMapUrl()) + "\">" + escape(locationName) + "</a>";
        }

        return escape(locationName);
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
                <span class="eyebrow">ការរំលឹក &nbsp;&middot;&nbsp; Reminder</span>
              </div>
              <div class="card">
                <div class="motif" aria-hidden="true">&nbsp;</div>
                <div class="head">
                  <p class="kicker">កម្មវិធីនឹងប្រព្រឹត្តទៅនាថ្ងៃស្អែក</p>
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
