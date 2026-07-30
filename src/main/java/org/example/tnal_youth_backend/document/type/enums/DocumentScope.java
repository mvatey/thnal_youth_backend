package org.example.tnal_youth_backend.document.type.enums;

/**
 * Separates document types between the two Document UX/UI tabs.
 *
 * INSTITUTIONAL:
 * - ឯកសារស្ថាប័ន
 *
 * MEMBER:
 * - ឯកសារផ្ទាល់ខ្លួនរបស់សមាជិក
 */
public enum DocumentScope {

    INSTITUTIONAL(
            "ឯកសារស្ថាប័ន",
            "Institutional Documents"
    ),

    MEMBER(
            "ឯកសារផ្ទាល់ខ្លួនរបស់សមាជិក",
            "Member Documents"
    );

    private final String labelKm;
    private final String labelEn;

    DocumentScope(
            String labelKm,
            String labelEn
    ) {
        this.labelKm = labelKm;
        this.labelEn = labelEn;
    }

    public String getLabelKm() {
        return labelKm;
    }

    public String getLabelEn() {
        return labelEn;
    }
}