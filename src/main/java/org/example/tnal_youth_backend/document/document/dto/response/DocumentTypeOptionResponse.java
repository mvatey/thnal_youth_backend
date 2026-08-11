package org.example.tnal_youth_backend.document.document.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DocumentTypeOptionResponse(

        Short id,

        String code,

        @JsonProperty("name_km")
        String nameKm,

        @JsonProperty("name_en")
        String nameEn
) {
}