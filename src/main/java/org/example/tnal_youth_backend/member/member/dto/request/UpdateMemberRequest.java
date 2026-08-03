package org.example.tnal_youth_backend.member.member.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record UpdateMemberRequest(

        @JsonProperty("member_no")
        @NotBlank(message = "Member number is required")
        @Size(
                max = 50,
                message = "Member number must not exceed 50 characters"
        )
        String memberNo,

        @JsonProperty("full_name_km")
        @NotBlank(message = "Khmer full name is required")
        @Size(
                max = 255,
                message = "Khmer full name must not exceed 255 characters"
        )
        String fullNameKm,

        @JsonProperty("full_name_en")
        @Size(max = 255)
        String fullNameEn,

        @JsonProperty("branch_id")
        @NotNull(message = "Branch ID is required")
        Long branchId,

        @JsonProperty("status_id")
        @NotNull(message = "Member status ID is required")
        Short statusId,

        @JsonProperty("level_id")
        Short levelId,

        @JsonProperty("religion_id")
        Short religionId,

        @JsonProperty("gender")
        @NotNull(message = "Gender is required")
        Gender gender,

        @JsonProperty("date_of_birth")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @JsonProperty("place_of_birth")
        String placeOfBirth,

        @JsonProperty("phone")
        @Size(max = 30)
        String phone,

        @JsonProperty("email")
        @Email(message = "Email format is invalid")
        String email,

        @JsonProperty("current_address")
        String currentAddress,

        @JsonProperty("permanent_address")
        String permanentAddress,

        @JsonProperty("profile_photo_id")
        Long profilePhotoId,

        @JsonProperty("cv_file_id")
        Long cvFileId,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        @JsonProperty("bio")
        String bio
) {
}