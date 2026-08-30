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
 * Renders the "certificate ready" email as the same bilingual letter
 * {@link CertificateReadyTelegramBuilder} sends to Telegram, wrapped in the
 * org's card-style HTML/CSS design shared with
 * {@link ActivityInvitationEmailBuilder}. {@code organizerBranch} is the
 * activity's host branch, named explicitly since the recipient (a
 * co-hosting branch's staff) may staff more than one branch.
 */
@Component
public class CertificateReadyEmailBuilder {

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
        String titleKm = escape(activity.getTitleKm());
        String activityNameEn = hasText(activity.getTitleEn()) ? escape(activity.getTitleEn()) : titleKm;

        String rawOrganizerNameKm = organizerBranch != null && hasText(organizerBranch.getNameKm())
                ? organizerBranch.getNameKm() : "";
        String organizerLabelKm = BranchLabels.withBranchPrefixKm(rawOrganizerNameKm);
        String organizerNameEn = organizerBranch != null && hasText(organizerBranch.getNameEn())
                ? organizerBranch.getNameEn() : rawOrganizerNameKm;
        String organizerLabelEn = BranchLabels.withBranchPrefixEn(organizerNameEn);

        String dateKm = activity.getStartsAt() == null ? "" : formatKhmerDate(activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET));
        String dateEn = activity.getStartsAt() == null ? "" : formatEnglishDate(activity.getStartsAt().withOffsetSameInstant(CAMBODIA_OFFSET));

        String contact = organizerBranch != null && (hasText(organizerBranch.getPhone()) || hasText(organizerBranch.getEmail()))
                ? Stream.of(organizerBranch.getPhone(), organizerBranch.getEmail())
                        .filter(CertificateReadyEmailBuilder::hasText)
                        .map(this::escape)
                        .collect(Collectors.joining(" &nbsp;&middot;&nbsp; "))
                : "";

        StringBuilder letterKm = new StringBuilder();
        letterKm.append("<p>ជម្រាបសួរ, ").append(escape(name)).append("</p>");
        letterKm.append("<p>អបអរសាទរ! អ្នកទទួលបានវិញ្ញាបនបត្របញ្ជាក់អំពីការចូលរួមរបស់អ្នកក្នុងកម្មវិធី &laquo;").append(titleKm).append("&raquo;");
        if (hasText(dateKm)) {
            letterKm.append(" ដែលបានប្រព្រឹត្តទៅនៅថ្ងៃទី ").append(dateKm);
        }
        letterKm.append(" របស់").append(hasText(organizerLabelKm) ? escape(organizerLabelKm) : "សាខាយើងខ្ញុំ").append("។</p>");
        letterKm.append("<p>អ្នកអាចចូលទៅទាញយកឯកសារនៅក្នុងគេហទំព័រយុវជនបាន។</p>");
        letterKm.append("<p>សូមគោរពអរគុណយ៉ាងក្រៃលែងចំពោះវត្តមានដ៏ខ្ពង់ខ្ពស់របស់អ្នកនៅក្នុងកម្មវិធី។</p>");
        letterKm.append("<p>ពី").append(hasText(organizerLabelKm) ? escape(organizerLabelKm) : "TNAL Youth Cambodia").append("</p>");

        StringBuilder letterEn = new StringBuilder();
        letterEn.append("<p>Dear ").append(escape(name)).append(",</p>");
        letterEn.append("<p>Congratulations! You have received a certificate confirming your participation in the activity &laquo;").append(activityNameEn).append("&raquo;");
        if (hasText(dateEn)) {
            letterEn.append(", held on ").append(dateEn);
        }
        letterEn.append(", from ").append(hasText(organizerLabelEn) ? organizerLabelEn : "our branch").append(".</p>");
        letterEn.append("<p>You may log in to the youth website to download the document.</p>");
        letterEn.append("<p>We sincerely thank you for your valued presence at the activity.</p>");
        letterEn.append("<p>From ").append(hasText(organizerLabelEn) ? organizerLabelEn : "TNAL Youth Cambodia").append("</p>");

        return TEMPLATE.formatted(
                titleKm,
                titleKm,
                activityNameEn,
                "វិញ្ញាបនបត្របានរួចរាល់ &middot; Certificate Ready",
                letterKm.toString(),
                letterEn.toString(),
                hasText(contact) ? "<p class=\"contact\">" + contact + "</p>" : ""
        );
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
                <span class="eyebrow">វិញ្ញាបនបត្រ &nbsp;&middot;&nbsp; Certificate</span>
              </div>
              <div class="card">
                <div class="motif" aria-hidden="true">&nbsp;</div>
                <div class="head">
                  <p class="kicker">វិញ្ញាបនបត្របញ្ជាក់ការចូលរួម</p>
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
