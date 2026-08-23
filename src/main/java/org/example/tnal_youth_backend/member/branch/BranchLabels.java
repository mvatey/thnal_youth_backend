package org.example.tnal_youth_backend.member.branch;

/**
 * Branch names in this system are inconsistently self-descriptive — some
 * already read "សាខាកណ្ដាល" ("Kandal Branch"), others are just the bare
 * province/city name ("រាជធានីភ្នំពេញ"). Anywhere a message wants to say
 * "[the] X branch" in Khmer, blindly prepending "សាខា" produces
 * "សាខាសាខាកណ្ដាល" for the first kind — this only adds the word when the
 * name doesn't already carry it.
 */
public final class BranchLabels {

    private static final String PREFIX = "សាខា";

    private BranchLabels() {
    }

    public static String withBranchPrefixKm(String rawNameKm) {
        if (rawNameKm == null || rawNameKm.isBlank()) {
            return "";
        }

        return rawNameKm.startsWith(PREFIX) ? rawNameKm : PREFIX + rawNameKm;
    }
}
