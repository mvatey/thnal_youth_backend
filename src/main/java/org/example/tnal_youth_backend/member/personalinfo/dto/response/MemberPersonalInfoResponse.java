package org.example.tnal_youth_backend.member.personalinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.TshirtSize;

import java.time.LocalDate;

public record MemberPersonalInfoResponse(

        Long id,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        Gender gender,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

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

        @JsonProperty("branch_name_km")
        String branchNameKm,

        @JsonProperty("tshirt_size")
        TshirtSize tshirtSize,

        @JsonProperty("current_address")
        String currentAddress,

        @JsonProperty("permanent_address")
        String permanentAddress,

        @JsonProperty("cv_file_id")
        Long cvFileId,

        @JsonProperty("account_id")
        Long accountId,

        @JsonProperty("has_account")
        boolean hasAccount,

        @JsonProperty("account_role")
        UserRole accountRole,

        @JsonProperty("account_status")
        UserStatus accountStatus
) {
}