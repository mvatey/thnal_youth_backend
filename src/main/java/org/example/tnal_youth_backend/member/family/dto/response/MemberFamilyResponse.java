package org.example.tnal_youth_backend.member.family.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.member.family.entity.FamilyLifeStatus;
import org.example.tnal_youth_backend.member.family.entity.FamilyRelationship;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MemberFamilyResponse(

        Long id,

        @JsonProperty("member_id")
        Long memberId,

        FamilyRelationship relationship,

        @JsonProperty("full_name_km")
        String fullNameKm,

        @JsonProperty("full_name_en")
        String fullNameEn,

        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,

        String occupation,

        @JsonProperty("life_status")
        FamilyLifeStatus lifeStatus,

        String address,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}