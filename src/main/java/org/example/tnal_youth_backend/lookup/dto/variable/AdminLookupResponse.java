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

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}