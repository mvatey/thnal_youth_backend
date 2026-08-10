package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.member.member.entity.Gender;

public record BranchLeaderCandidateResponse(

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        String phone,

        String email,

        Gender gender,

        @JsonProperty("current_role")
        UserRole currentRole,

        @JsonProperty("profile_photo_id")
        Long profilePhotoId,

        @JsonProperty("has_account")
        boolean hasAccount
) {
}
