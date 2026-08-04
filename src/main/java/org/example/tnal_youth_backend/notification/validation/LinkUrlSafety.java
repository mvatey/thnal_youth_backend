package org.example.tnal_youth_backend.notification.validation;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

/**
 * Framework-free link-safety logic for notification {@code actionUrl} values.
 *
 * <p>The old {@code @Pattern("^(https?://|/).*")} accepted any string that merely
 * <em>started</em> with {@code /} or {@code http(s)://}, which let open-redirect /
 * phishing links through the notification tray:
 * <ul>
 *   <li>{@code //evil.example.com/phish} — protocol-relative, navigates off-site</li>
 *   <li>{@code /\evil.example.com} — backslash is normalised to {@code /} by browsers</li>
 *   <li>{@code https://evil.example.com/phish} — any external host</li>
 * </ul>
 *
 * <p>Rules enforced here:
 * <ol>
 *   <li>{@code null} / blank is valid (the field is optional).</li>
 *   <li>Backslashes are normalised to forward slashes before any decision.</li>
 *   <li>Protocol-relative ({@code //host/...}) is rejected.</li>
 *   <li>App-relative paths ({@code /programs/12}) are allowed — they stay on our origin.</li>
 *   <li>Absolute URLs must parse, use {@code http}/{@code https}, and have a host on the
 *       configured allow-list.</li>
 * </ol>
 *
 * <p>Kept as a static, dependency-free utility so it can be unit-tested with plain
 * JUnit (no Spring context) and reused anywhere a link needs vetting.
 */
public final class LinkUrlSafety {

    private LinkUrlSafety() {
    }

    /**
     * @param value        the candidate link (may be {@code null})
     * @param allowedHosts lowercase hosts an absolute URL may target
     * @return {@code true} if the link is safe to persist and render
     */
    public static boolean isSafe(String value, Set<String> allowedHosts) {
        // Optional field: nothing to validate.
        if (value == null || value.isBlank()) {
            return true;
        }

        // Browsers treat "\" as "/", so normalise first — otherwise "/\evil.com"
        // would smuggle a protocol-relative URL past the "starts with /" check.
        String v = value.strip().replace('\\', '/');

        // Protocol-relative ("//host/...") navigates off our origin. Reject.
        if (v.startsWith("//")) {
            return false;
        }

        // App-relative path ("/programs/12") stays on our origin. Allow.
        if (v.startsWith("/")) {
            return true;
        }

        // Otherwise it must be an absolute URL we can parse and vet.
        final URI uri;
        try {
            uri = new URI(v);
        } catch (Exception e) {
            // Unparseable (e.g. "data:text/html,<script>...") -> unsafe.
            return false;
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            // Bare "evil.example.com" with no scheme -> ambiguous -> unsafe.
            return false;
        }
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            return false;
        }

        String host = uri.getHost();
        if (host == null) {
            // Malformed authority (getHost() strips any "user@" userinfo, so
            // "https://evil@cyna.org/x" correctly resolves host = cyna.org).
            return false;
        }
        host = host.toLowerCase(Locale.ROOT);

        return allowedHosts.contains(host);
    }
}
