package org.example.tnal_youth_backend.member.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.file.dto.response.FileResponse;
import org.example.tnal_youth_backend.member.ethnicity.dto.response.EthnicityResponse;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.nationality.dto.response.NationalityResponse;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MemberDetailResponse(

        Long id,

        @JsonProperty("member_no")
        String memberNo,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        @JsonProperty("branch_id")
        Long branchId,

        LookupResponse status,

        LookupResponse level,

        LookupResponse religion,

        NationalityResponse nationality,

        EthnicityResponse ethnicity,

        Gender gender,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        @JsonProperty("place_of_birth")
        String placeOfBirth,

        String tshirtSize,

        String phone,

        String email,

        @JsonProperty("current_address")
        String currentAddress,

        @JsonProperty("permanent_address")
        String permanentAddress,

        @JsonProperty("profile_photo")
        FileResponse profilePhoto,

        @JsonProperty("cv_file")
        FileResponse cvFile,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        String bio,

        @JsonProperty("created_by")
        Long createdById,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
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
}