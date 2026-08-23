package org.example.tnal_youth_backend.lookup.enums;

import lombok.Getter;

@Getter
public enum LookupCategory {

    ACTIVITY_TYPE(
            "activity-types",
            "ប្រភេទសកម្មភាព",
            "Activity Type"
    ),

    ACTIVITY_SECTOR(
            "activity-sectors",
            "វិស័យសកម្មភាព",
            "Activity Sector"
    ),

    MEMBER_LEVEL(
            "member-levels",
            "កម្រិតសមាជិក",
            "Member Level"
    ),

    NATIONALITY(
            "nationalities",
            "សញ្ជាតិ",
            "Nationality"
    ),

    RELIGION(
            "religions",
            "សាសនា",
            "Religion"
    ),

    EDUCATION_LEVEL(
            "education-levels",
            "កម្រិតការសិក្សា",
            "Education Level"
    ),

    LANGUAGE(
            "languages",
            "ភាសា",
            "Language"
    ),

    SKILL(
            "skills",
            "ជំនាញកុំព្យូទ័រ",
            "Computer Skill"
    ),

    PROFICIENCY_LEVEL(
            "proficiency-levels",
            "កម្រិតជំនាញ",
            "Proficiency Level"
    ),

    DOCUMENT_TYPE(
            "document-types",
            "ប្រភេទឯកសារ",
            "Document Type"
    ),

    ETHNICITY(
            "ethnicities",
            "ជនជាតិ",
            "Ethnicity"
    ),

    PAYMENT_METHOD(
            "payment-methods",
            "វិធីបង់ប្រាក់",
            "Payment Method"
    ),

    POSITION(
            "positions",
            "តំណែង",
            "Position"
    ),

    POLITICAL_PARTY(
            "political-parties",
            "គណបក្សនយោបាយ",
            "Political Party"
    );

    private final String path;
    private final String labelKm;
    private final String labelEn;

    LookupCategory(
            String path,
            String labelKm,
            String labelEn
    ) {
        this.path = path;
        this.labelKm = labelKm;
        this.labelEn = labelEn;
    }

    public static LookupCategory fromPath(
            String path
    ) {

        if (
                path == null
                        || path.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Lookup category is required"
            );
        }

        String normalized =
                path.trim();

        for (
                LookupCategory category
                : values()
        ) {

            if (
                    category.getPath()
                            .equalsIgnoreCase(
                                    normalized
                            )
            ) {
                return category;
            }
        }

        throw new IllegalArgumentException(
                "Unsupported lookup category: "
                        + path
        );
    }
}