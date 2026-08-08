package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.authentication.model.entity.Role;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record BranchLeaderResponse(

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        String phone,

        String email,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        @JsonProperty("joined_on")
        LocalDate joinedOn,

        Gender gender,

        UserRole role
) {
}