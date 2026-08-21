package org.example.tnal_youth_backend.member.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record CreateMemberRequest(
        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        Gender gender,

        @JsonProperty("nationality_id")
        Short nationalityId,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        String phone,
        String email,

        @JsonProperty("branch_id")
        Long branchId,

        @JsonProperty("level_id")
        Short levelId,

        UserRole role,

        @JsonProperty("position_id")
        Short positionId,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        @JsonProperty("status_id")
        Short statusId,

        @JsonProperty("profile_photo_id")
        Long profilePhotoId
) {
}
