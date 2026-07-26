package org.example.tnal_youth_backend.activity.activity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ActivityListResponse(

        Long id,

        String titleKm,

        String titleEn,

        LookupShort type,

        LookupShort sector,

        LookupShort status,

        LookupLong branch,

        Boolean isPublic,

        OffsetDateTime startsAt,

        OffsetDateTime endsAt,

        String locationName
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LookupShort(
            Short id,
            String code,
            String labelKm
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LookupLong(
            Long id,
            String nameKm
    ) {
    }
}
