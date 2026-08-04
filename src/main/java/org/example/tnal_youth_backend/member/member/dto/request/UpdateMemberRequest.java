package org.example.tnal_youth_backend.member.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record UpdateMemberRequest(
        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        Gender gender,

        @JsonProperty("nationality_id")
        Short nationalityId,

        @JsonProperty("religion_id")
        Short religionId,

        @JsonProperty("ethnicity_id")
        Short ethnicityId,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        @JsonProperty("place_of_birth")
        String placeOfBirth,

        String phone,
        String email,

        @JsonProperty("branch_id")
        Long branchId,

        @JsonProperty("level_id")
        Short levelId,

        @JsonProperty("status_id")
        Short statusId,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        @JsonProperty("current_address")
        String currentAddress,

        @JsonProperty("permanent_address")
        String permanentAddress,

        @JsonProperty("profile_photo_id")
        Long profilePhotoId,

        @JsonProperty("cv_file_id")
        Long cvFileId,

        @JsonProperty("tshirt_size")
        String tshirtSize,

        String bio
) {
}