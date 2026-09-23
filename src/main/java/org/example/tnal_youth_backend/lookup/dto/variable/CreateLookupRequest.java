package org.example.tnal_youth_backend.lookup.dto.variable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLookupRequest(

        @NotBlank
        @Size(max = 150)
        String labelKm,

        @Size(max = 150)
        String labelEn,

        String description,

        @NotNull
        Boolean active,

        /*
         * Only used for PAYMENT_METHOD. Must be CASH, BANK, or OTHER
         * (defaults to OTHER when blank). Ignored for every other
         * category.
         */
        String category,

        /*
         * Only used for POSITION. Must be BRANCH_LEADER, SECRETARY,
         * MEMBER, or VIEWER when provided; left null/blank means this
         * position has no auto-assigned role. Ignored for every other
         * category.
         */
        String mappedRole,

        /*
         * Only used for POSITION when mappedRole is VIEWER. Must be
         * BRANCH_LEADER or SECRETARY when provided. Ignored for every
         * other mappedRole/category.
         */
        String mappedViewerScope

) {
}