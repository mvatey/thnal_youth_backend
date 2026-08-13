package org.example.tnal_youth_backend.lookup.dto.variable;

public record LookupCategoryResponse(

        String code,

        String path,

        String labelKm,

        String labelEn,

        long count

) {
}