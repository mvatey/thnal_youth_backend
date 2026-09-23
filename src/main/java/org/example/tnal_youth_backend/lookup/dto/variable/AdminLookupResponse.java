package org.example.tnal_youth_backend.lookup.dto.variable;

import java.time.OffsetDateTime;

public record AdminLookupResponse(

        Short id,

        String code,

        String labelKm,

        String labelEn,

        String description,

        Boolean active,

        Integer sortOrder,

        /*
         * Only meaningful for PAYMENT_METHOD (CASH / BANK / OTHER).
         * Null for every other category.
         */
        String category,

        /*
         * Only meaningful for POSITION (BRANCH_LEADER / SECRETARY /
         * MEMBER / VIEWER). Null for every other category, and for a
         * position with no auto-assigned role.
         */
        String mappedRole,

        /*
         * Only meaningful for POSITION when mappedRole is VIEWER
         * (BRANCH_LEADER / SECRETARY). Null otherwise.
         */
        String mappedViewerScope,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}