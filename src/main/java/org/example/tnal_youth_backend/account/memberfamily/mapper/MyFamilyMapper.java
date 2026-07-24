package org.example.tnal_youth_backend.account.memberfamily.mapper;

import org.example.tnal_youth_backend.account.memberfamily.dto.request.MyFamilyRequest;
import org.example.tnal_youth_backend.account.memberfamily.dto.response.MyFamilyResponse;
import org.example.tnal_youth_backend.member.family.entity.MemberFamily;
import org.springframework.stereotype.Component;

@Component
public class MyFamilyMapper {

    public MyFamilyResponse toResponse(
            MemberFamily family
    ) {
        return new MyFamilyResponse(
                family.getId(),

                family.getMember() != null
                        ? family.getMember().getId()
                        : null,

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

    public void updateEntity(
            MemberFamily family,
            MyFamilyRequest request
    ) {
        family.setRelationship(
                request.relationship()
        );

        family.setFullNameKm(
                normalizeRequired(request.fullNameKm())
        );

        family.setFullNameEn(
                normalizeOptional(request.fullNameEn())
        );

        family.setDateOfBirth(
                request.dateOfBirth()
        );

        family.setOccupation(
                normalizeOptional(request.occupation())
        );

        family.setLifeStatus(
                request.lifeStatus()
        );

        family.setAddress(
                normalizeOptional(request.address())
        );
    }

    private String normalizeRequired(
            String value
    ) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private String normalizeOptional(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}