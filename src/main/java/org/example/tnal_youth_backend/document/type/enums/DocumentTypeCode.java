package org.example.tnal_youth_backend.document.type.enums;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * All document types supported by the complete Document module.
 *
 * Institutional documents:
 * - ORGANIZATION_DOCUMENT
 * - BRANCH_DOCUMENT
 * - ACTIVITY_DOCUMENT
 *
 * Member personal Create UI:
 * - MEMBER_CARD
 * - MEMBER_LETTER
 * - MEMBER_CERTIFICATE
 * - MEMBER_DOCUMENT
 *
 * Activity certificate:
 * - ACTIVITY_CERTIFICATE
 *
 * ACTIVITY_CERTIFICATE belongs to a member, but it additionally requires an
 * activity owner. Therefore, it is not created by the current Member Personal
 * Document Create screen.
 */
public enum DocumentTypeCode {

    ORGANIZATION_DOCUMENT(
            DocumentScope.INSTITUTIONAL,
            "ឯកសារអង្គភាព",
            "Organization Document"
    ),

    BRANCH_DOCUMENT(
            DocumentScope.INSTITUTIONAL,
            "ឯកសារសាខា",
            "Branch Document"
    ),

    ACTIVITY_DOCUMENT(
            DocumentScope.INSTITUTIONAL,
            "ឯកសារសកម្មភាព",
            "Activity Document"
    ),

    MEMBER_CARD(
            DocumentScope.MEMBER,
            "ប័ណ្ណសមាជិក",
            "Member Card"
    ),

    MEMBER_LETTER(
            DocumentScope.MEMBER,
            "លិខិតសមាជិក",
            "Member Letter"
    ),

    MEMBER_CERTIFICATE(
            DocumentScope.MEMBER,
            "វិញ្ញាបនបត្រសមាជិក",
            "Member Certificate"
    ),

    ACTIVITY_CERTIFICATE(
            DocumentScope.MEMBER,
            "វិញ្ញាបនបត្រសកម្មភាព",
            "Activity Certificate"
    ),

    MEMBER_DOCUMENT(
            DocumentScope.MEMBER,
            "ឯកសារសមាជិក",
            "Member Document"
    );

    /**
     * Document types belonging to the Institutional module.
     */
    private static final Set<DocumentTypeCode> INSTITUTIONAL_TYPES =
            EnumSet.of(
                    ORGANIZATION_DOCUMENT,
                    BRANCH_DOCUMENT,
                    ACTIVITY_DOCUMENT
            );

    /**
     * Every type whose owner includes a member.
     *
     * ACTIVITY_CERTIFICATE is included because it belongs to both:
     *
     * member_id   = selected member
     * activity_id = selected activity
     */
    private static final Set<DocumentTypeCode> MEMBER_TYPES =
            EnumSet.of(
                    MEMBER_CARD,
                    MEMBER_LETTER,
                    MEMBER_CERTIFICATE,
                    ACTIVITY_CERTIFICATE,
                    MEMBER_DOCUMENT
            );

    /**
     * Types supported by the current two Member Document Create UI screens.
     *
     * ACTIVITY_CERTIFICATE is intentionally excluded because that type
     * requires activityId, and the current UI does not provide an activity.
     */
    private static final Set<DocumentTypeCode>
            MEMBER_PERSONAL_CREATE_TYPES =
            EnumSet.of(
                    MEMBER_CARD,
                    MEMBER_LETTER,
                    MEMBER_CERTIFICATE,
                    MEMBER_DOCUMENT
            );

    private final DocumentScope scope;
    private final String labelKm;
    private final String labelEn;

    DocumentTypeCode(
            DocumentScope scope,
            String labelKm,
            String labelEn
    ) {
        this.scope = scope;
        this.labelKm = labelKm;
        this.labelEn = labelEn;
    }

    public DocumentScope getScope() {
        return scope;
    }

    public String getLabelKm() {
        return labelKm;
    }

    public String getLabelEn() {
        return labelEn;
    }

    // =========================================================
    // CODE PARSING
    // =========================================================

    public static DocumentTypeCode fromCode(
            String code
    ) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Document type code must not be empty"
            );
        }

        String normalizedCode =
                code
                        .trim()
                        .toUpperCase(Locale.ROOT);

        try {
            return DocumentTypeCode.valueOf(
                    normalizedCode
            );

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported document type code: "
                            + code,
                    exception
            );
        }
    }

    public static DocumentTypeCode fromCodeOrNull(
            String code
    ) {
        try {
            return fromCode(code);

        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    // =========================================================
    // SCOPE HELPERS
    // =========================================================

    public boolean isInstitutional() {
        return INSTITUTIONAL_TYPES.contains(this);
    }

    public boolean isMember() {
        return MEMBER_TYPES.contains(this);
    }

    /**
     * Returns true only for types accepted by:
     *
     * POST /api/documents/members
     */
    public boolean isMemberPersonalCreateType() {
        return MEMBER_PERSONAL_CREATE_TYPES.contains(this);
    }

    public boolean isOrganizationDocument() {
        return this == ORGANIZATION_DOCUMENT;
    }

    public boolean isBranchDocument() {
        return this == BRANCH_DOCUMENT;
    }

    public boolean isActivityDocument() {
        return this == ACTIVITY_DOCUMENT;
    }

    public boolean isMemberCard() {
        return this == MEMBER_CARD;
    }

    public boolean isMemberLetter() {
        return this == MEMBER_LETTER;
    }

    public boolean isMemberCertificate() {
        return this == MEMBER_CERTIFICATE;
    }

    public boolean isActivityCertificate() {
        return this == ACTIVITY_CERTIFICATE;
    }

    public boolean isMemberDocument() {
        return this == MEMBER_DOCUMENT;
    }

    public boolean isCertificate() {
        return this == MEMBER_CERTIFICATE
                || this == ACTIVITY_CERTIFICATE;
    }

    // =========================================================
    // REQUIRED OWNER HELPERS
    // =========================================================

    public boolean requiresBranch() {
        return this == BRANCH_DOCUMENT;
    }

    public boolean requiresMember() {
        return MEMBER_TYPES.contains(this);
    }

    public boolean requiresActivity() {
        return this == ACTIVITY_DOCUMENT
                || this == ACTIVITY_CERTIFICATE;
    }

    // =========================================================
    // SET HELPERS
    // =========================================================

    public static Set<DocumentTypeCode> institutionalTypes() {
        return Set.copyOf(
                INSTITUTIONAL_TYPES
        );
    }

    public static Set<DocumentTypeCode> memberTypes() {
        return Set.copyOf(
                MEMBER_TYPES
        );
    }

    public static Set<DocumentTypeCode> memberPersonalCreateTypes() {
        return Set.copyOf(
                MEMBER_PERSONAL_CREATE_TYPES
        );
    }

    public static Set<String> institutionalCodeNames() {
        return INSTITUTIONAL_TYPES
                .stream()
                .map(Enum::name)
                .collect(
                        Collectors.toUnmodifiableSet()
                );
    }

    public static Set<String> memberCodeNames() {
        return MEMBER_TYPES
                .stream()
                .map(Enum::name)
                .collect(
                        Collectors.toUnmodifiableSet()
                );
    }

    public static Set<String> memberPersonalCreateCodeNames() {
        return MEMBER_PERSONAL_CREATE_TYPES
                .stream()
                .map(Enum::name)
                .collect(
                        Collectors.toUnmodifiableSet()
                );
    }
}