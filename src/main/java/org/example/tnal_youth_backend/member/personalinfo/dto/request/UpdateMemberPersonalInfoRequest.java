package org.example.tnal_youth_backend.member.personalinfo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.TshirtSize;

import java.time.LocalDate;

public record UpdateMemberPersonalInfoRequest(

        @NotBlank(message = "Khmer full name is required")
        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        Gender gender,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        @Email(message = "Email format is invalid")
        String email,

        String phone,

        @JsonProperty("religion_id")
        Short religionId,

        @JsonProperty("ethnicity_id")
        Short ethnicityId,

        @JsonProperty("nationality_id")
        Short nationalityId,

        @JsonProperty("member_level_id")
        Short memberLevelId,

        @JsonProperty("branch_id")
        Long branchId,

        @JsonProperty("tshirt_size")
        TshirtSize tshirtSize,

        @JsonProperty("current_address")
        String currentAddress,

        @JsonProperty("permanent_address")
        String permanentAddress
) {
}