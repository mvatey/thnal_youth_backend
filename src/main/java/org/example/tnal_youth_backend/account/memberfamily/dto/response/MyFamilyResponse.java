package org.example.tnal_youth_backend.account.memberfamily.dto.response;

import org.example.tnal_youth_backend.member.family.entity.FamilyLifeStatus;
import org.example.tnal_youth_backend.member.family.entity.FamilyRelationship;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MyFamilyResponse(

        Long id,

        Long memberId,

        FamilyRelationship relationship,

        String fullNameKm,

        String fullNameEn,

        LocalDate dateOfBirth,

        String occupation,

        FamilyLifeStatus lifeStatus,

        String address,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}