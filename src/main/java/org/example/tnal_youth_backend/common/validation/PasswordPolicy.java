package org.example.tnal_youth_backend.common.validation;

/*
 * Shared password strength rule, used by every request DTO that accepts a
 * new password (@Pattern(regexp = PasswordPolicy.REGEX, ...)) and by the
 * two hand-written service-layer checks that duplicate DTO validation
 * (MyAccountServiceImpl, MemberPasswordServiceImpl) -- one place to change
 * the rule instead of N copies drifting apart.
 */
public final class PasswordPolicy {

    /**
     * At least 6 characters, at least one digit, at least one non
     * alphanumeric character.
     */
    public static final String REGEX =
            "^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,}$";

    /**
     * Same as REGEX, but also accepts an empty string -- for optional
     * "leave blank to keep unchanged" password fields (e.g.
     * UpdateUserRequest), where blank must stay valid.
     */
    public static final String REGEX_OR_BLANK =
            "^$|" + REGEX;

    public static final String MESSAGE =
            "Password must be at least 6 characters and include "
                    + "at least one number and one symbol";

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null && password.matches(REGEX);
    }
}
