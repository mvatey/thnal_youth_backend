package org.example.tnal_youth_backend.lookup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BranchStatusOptionResponse(

        Short id,

        String code,

        @JsonProperty("name_km")
        String nameKm,

        @JsonProperty("name_en")
        String nameEn
) {
}
