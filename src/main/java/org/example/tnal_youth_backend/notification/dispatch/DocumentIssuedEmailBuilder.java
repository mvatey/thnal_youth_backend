package org.example.tnal_youth_backend.notification.dispatch;

import org.example.tnal_youth_backend.document.document.entity.Document;
import org.springframework.stereotype.Component;

/**
 * Renders the "document issued" email as the same bilingual letter
 * {@link DocumentIssuedTelegramBuilder} sends to Telegram, wrapped in the
 * org's card-style HTML/CSS design shared with
 * {@link ActivityInvitationEmailBuilder}. Unlike the activity-related
 * builders, there is no separate English document title in the schema —
 * {@link Document#getTitle()} is shown as-is in both languages.
 */
@Component
public class DocumentIssuedEmailBuilder {

    public String build(Document document, String recipientNameKm) {
        String name = hasText(recipientNameKm) ? recipientNameKm : "សមាជិក";
        String title = escape(document.getTitle());

        StringBuilder letterKm = new StringBuilder();
        letterKm.append("<p>ជម្រាបសួរ ").append(escape(name)).append("</p>");
        letterKm.append("<p>សូមស្វាគមន៍ក្នុងការចូលរួមជាមួយសមាគមថ្នាលយុវជនកម្ពុជា។</p>");
        letterKm.append("<p>អ្នកទទួលបាន &laquo;").append(title).append("&raquo; ដែលបញ្ជាក់អំពីការចូលរួមរបស់អ្នក។ សូមចូលទៅកាន់គណនីរបស់អ្នកដើម្បីមើលឯកសារ។</p>");
        letterKm.append("<p>ពីថ្នាលយុវជនកម្ពុជា</p>");

        StringBuilder letterEn = new StringBuilder();
        letterEn.append("<p>Dear ").append(escape(name)).append(",</p>");
        letterEn.append("<p>Welcome to the Cambodian Youth Nursery Association (TNAL Youth).</p>");
        letterEn.append("<p>You have received &laquo;").append(title).append("&raquo;, confirming your participation. Please log in to your account to view the document.</p>");
        letterEn.append("<p>From TNAL Youth Cambodia</p>");

        return TEMPLATE.formatted(
                title,
                title,
                title,
                "ឯកសារត្រូវបានចេញ &middot; Document Issued",
                letterKm.toString(),
                letterEn.toString()
        );
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
                <span class="eyebrow">ឯកសារ &nbsp;&middot;&nbsp; Document</span>
              </div>
              <div class="card">
                <div class="motif" aria-hidden="true">&nbsp;</div>
                <div class="head">
                  <p class="kicker">ឯកសារត្រូវបានចេញ</p>
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
              </div>
            </div>
            </td></tr></table>
            </body>
            </html>
            """;
}
