package org.example.tnal_youth_backend.authentication.util;

/*
 * A login identifier (the single "phone, email, or username" field) needs
 * the same normalization wherever it's looked up, or two lookups for the
 * exact same account can disagree -- see AuthServiceImpl#login, which used
 * to skip this and matched emails case-sensitively while
 * AccountStatusServiceImpl (the "does this account exist" pre-check the
 * login page calls before showing the password field) already lowercased
 * them. A capitalized email then passed that pre-check -- accounts store
 * email lowercase, see MemberServiceImpl/UserManagementServiceImpl -- but
 * failed the real login with "invalid credentials" no matter how correct
 * the password was, since the case-sensitive query never found the row at
 * all.
 */
public final class LoginIdentifierNormalizer {

    private LoginIdentifierNormalizer() {
    }

    public static String normalize(String identifier) {
        String trimmed = identifier.trim();

        if (trimmed.contains("@")) {
            return trimmed.toLowerCase();
        }

        // A username (e.g. "Phan Rithy") contains letters/spaces that
        // PhoneNumberUtil.toDatabaseFormat rejects outright -- only run it
        // through phone normalization when the input actually looks like
        // one, otherwise pass it through as-is for the username match.
        if (looksLikePhoneNumber(trimmed)) {
            return PhoneNumberUtil.toDatabaseFormat(trimmed);
        }

        return trimmed;
    }

    private static boolean looksLikePhoneNumber(String value) {
        return value.matches("^[0-9+()\\- ]+$");
    }
}
