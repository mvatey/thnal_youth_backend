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
        String category

) {
}