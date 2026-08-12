package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record BranchMemberTableItemResponse(

        Long id,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        String phone,

        String email,

        Gender gender,

        UserRole role,

        @JsonProperty("status_id")
        Short statusId,

        @JsonProperty("status_code")
        String statusCode,

        @JsonProperty("status_label_km")
        String statusLabelKm,

        @JsonProperty("status_label_en")
        String statusLabelEn,

        @JsonProperty("level_id")
        Short levelId,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        @JsonProperty("profile_photo_id")
        Long profilePhotoId

) {
}