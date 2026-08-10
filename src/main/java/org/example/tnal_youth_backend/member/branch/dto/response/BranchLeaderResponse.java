package org.example.tnal_youth_backend.member.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

        UserRole role,

        String status,

        @JsonProperty("profile_photo_id")
        Long profilePhotoId,

        @JsonProperty("profile_image")
        String profileImage
) {
    /**
     * Compatibility constructor for the JDBC-backed branch-leader endpoint.
     * It retains the richer status/photo payload introduced by the other
     * branch while exposing the same canonical response shape everywhere.
     */
    public BranchLeaderResponse(
            Long memberId,
            String fullNameKm,
            String fullNameEn,
            String gender,
            String status,
            String phone,
            String email,
            LocalDate dateOfBirth,
            LocalDate joinedOn,
            Long profilePhotoId,
            String profileImage,
            String role
    ) {
        this(
                memberId,
                fullNameKm,
                fullNameEn,
                phone,
                email,
                dateOfBirth,
                joinedOn,
                gender == null ? null : Gender.valueOf(gender),
                role == null ? null : UserRole.BRANCH_LEADER,
                status,
                profilePhotoId,
                profileImage
        );
    }
}
