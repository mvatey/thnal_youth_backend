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
        String category

) {
}