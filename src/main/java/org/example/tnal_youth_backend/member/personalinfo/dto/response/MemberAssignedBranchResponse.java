package org.example.tnal_youth_backend.member.personalinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberAssignedBranchResponse(

        Long id,

        @JsonProperty("name_km")
        String nameKm,

        @JsonProperty("name_en")
        String nameEn
) {
}