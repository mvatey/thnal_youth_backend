package org.example.tnal_youth_backend.member.family.mapper;

import org.example.tnal_youth_backend.member.family.dto.response.FamilyPersonResponse;
import org.example.tnal_youth_backend.member.family.entity.MemberFamily;
import org.springframework.stereotype.Component;

@Component
public class MemberFamilyMapper {

    public FamilyPersonResponse toResponse(
            MemberFamily family
    ) {
        if (family == null) {
            return null;
        }

        return new FamilyPersonResponse(
                family.getId(),
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