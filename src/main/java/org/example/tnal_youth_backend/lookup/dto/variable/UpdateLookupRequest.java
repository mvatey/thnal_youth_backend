package org.example.tnal_youth_backend.lookup.dto.variable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLookupRequest(

        @NotBlank
        @Size(max = 150)
        String labelKm,

        @Size(max = 150)
        String labelEn,

        String description,

        Integer sortOrder,

        /*
         * Only used for PAYMENT_METHOD. Must be CASH, BANK, or OTHER
         * when provided. Ignored for every other category.
         */
        String category,

        /*
         * Only used for POSITION. Must be BRANCH_LEADER, SECRETARY,
         * MEMBER, or VIEWER when provided, or blank/null to clear the
         * mapping. Ignored for every other category.
         */
        String mappedRole,

        /*
         * Only used for POSITION when mappedRole is VIEWER. Must be
         * BRANCH_LEADER or SECRETARY when provided, or blank/null to
         * clear it. Ignored for every other mappedRole/category.
         */
        String mappedViewerScope

) {
}