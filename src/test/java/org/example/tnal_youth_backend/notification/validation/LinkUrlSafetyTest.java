package org.example.tnal_youth_backend.notification.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic tests for {@link LinkUrlSafety} — no Spring context.
 *
 * <p>Mirrors the allow-list used in production defaults ({@code cyna.org},
 * {@code www.cyna.org}) and covers the open-redirect / scheme bypasses the old
 * {@code @Pattern} let through.
 */
class LinkUrlSafetyTest {

    private static final Set<String> ALLOW = Set.of("cyna.org", "www.cyna.org");

    @DisplayName("accepts app-relative paths and allowed http(s) hosts")
    @ParameterizedTest(name = "safe: {0}")
    @ValueSource(strings = {
            "/programs/12",
            "/members/45/edit",
            "https://cyna.org/programs/12",
            "https://www.cyna.org/",
            "http://cyna.org/x",
            "https://EVIL.example.com@cyna.org/x", // userinfo stripped -> host is cyna.org
            "   "                                  // blank -> nothing to validate
    })
    void accepts(String url) {
        assertThat(LinkUrlSafety.isSafe(url, ALLOW)).isTrue();
    }

    @DisplayName("null is accepted (optional field)")
    @ParameterizedTest
    @NullSource
    void acceptsNull(String url) {
        assertThat(LinkUrlSafety.isSafe(url, ALLOW)).isTrue();
    }

    @DisplayName("rejects protocol-relative, unsafe schemes and off-site hosts")
    @ParameterizedTest(name = "unsafe: {0}")
    @ValueSource(strings = {
            "//evil.example.com/phish",              // protocol-relative
            "/\\evil.example.com",                   // backslash normalised to //
            "javascript:alert(1)",                   // scheme bypass
            "data:text/html,<script>alert(1)</script>", // scheme bypass
            "https://evil.example.com/phish",        // off-site host
            "ftp://cyna.org/x",                       // non-http(s) scheme
            "evil.example.com"                        // bare host, no scheme
    })
    void rejects(String url) {
        assertThat(LinkUrlSafety.isSafe(url, ALLOW)).isFalse();
    }

    @DisplayName("host matching is case-insensitive")
    @ParameterizedTest
    @ValueSource(strings = {
            "https://CYNA.ORG/x",
            "https://Www.Cyna.Org/x"
    })
    void hostCaseInsensitive(String url) {
        assertThat(LinkUrlSafety.isSafe(url, ALLOW)).isTrue();
    }
}
