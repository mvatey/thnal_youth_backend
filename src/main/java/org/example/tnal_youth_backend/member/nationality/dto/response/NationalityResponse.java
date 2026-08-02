package org.example.tnal_youth_backend.member.nationality.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NationalityResponse(

        @JsonProperty("id")
        Short id,

        @JsonProperty("code")
        String code,

        @JsonProperty("label_km")
        String labelKm,

        @JsonProperty("label_en")
        String labelEn

) {
}