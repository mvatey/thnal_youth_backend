package org.example.tnal_youth_backend.lookup.dto.variable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLookupRequest(

        @NotBlank
        @Size(max = 150)
        String labelKm,

        /*
         * Required (not just optional) since it also doubles as the
         * source for this item's internal generated code (see
         * AdminLookupServiceImpl#generateCode) -- without it, a
         * Khmer-only label has no ASCII characters to build a
         * human-readable code from and falls back to an opaque random
         * one instead.
         */
        @NotBlank
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