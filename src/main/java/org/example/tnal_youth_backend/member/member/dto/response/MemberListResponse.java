package org.example.tnal_youth_backend.member.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record MemberListResponse(

        Long id,

        @JsonProperty("member_no")
        String memberNo,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        Gender gender,

        String phone,

        String email,

        @JsonProperty("branch_id")
        Long branchId,

        LookupResponse status,

        LookupResponse level,

        @JsonProperty("profile_photo")
        FileSummaryResponse profilePhoto,

        @JsonProperty("joined_on")
        LocalDate joinedOn
) {

    public record LookupResponse(
            Short id,
            String code,

            @JsonProperty("label_km")
            String labelKm,

            @JsonProperty("label_en")
            String labelEn
    ) {
    }

    public record FileSummaryResponse(
            Long id,

            @JsonProperty("file_path")
            String filePath,

            @JsonProperty("original_name")
            String originalName,

            @JsonProperty("mime_type")
            String mimeType
    ) {
    }
}