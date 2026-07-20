package org.example.tnal_youth_backend.member.family.mapper;

import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyResponse;
import org.example.tnal_youth_backend.member.family.entity.MemberFamily;
import org.springframework.stereotype.Component;

@Component
public class MemberFamilyMapper {

    public MemberFamilyResponse toResponse(
            MemberFamily family
    ) {
        if (family == null) {
            return null;
        }

        return new MemberFamilyResponse(
                family.getId(),
                family.getMember().getId(),
                family.getRelationship(),
                family.getFullNameKm(),
                family.getFullNameEn(),
                family.getDateOfBirth(),
                family.getOccupation(),
                family.getLifeStatus(),
                family.getAddress(),
                family.getCreatedAt(),
                family.getUpdatedAt()
        );
    }
}