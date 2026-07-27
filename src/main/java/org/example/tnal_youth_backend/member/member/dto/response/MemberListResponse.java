package org.example.tnal_youth_backend.member.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record MemberListResponse(

        Long id,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        GenderResponse gender,

        BranchResponse branch,

        LookupResponse status,

        LookupResponse level,

        @JsonProperty("profile_photo")
        ProfilePhotoResponse profilePhoto,

        @JsonProperty("joined_on")
        LocalDate joinedOn
) {

    public record GenderResponse(

            String code,

            @JsonProperty("label_km")
            String labelKm
    ) {
    }

    public record BranchResponse(

            Long id,

            @JsonProperty("label_km")
            String labelKm
    ) {
    }

    public record LookupResponse(

            Short id,

            String code,

            @JsonProperty("label_km")
            String labelKm,

            @JsonProperty("label_en")
            String labelEn
    ) {
    }

    public record ProfilePhotoResponse(

            Long id,

            String url
    ) {
    }
}