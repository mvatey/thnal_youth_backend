package org.example.tnal_youth_backend.member.politicalaffiliation.mapper;

import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.example.tnal_youth_backend.member.politicalaffiliation.entity.MemberPoliticalAffiliation;
import org.springframework.stereotype.Component;

@Component
public class MemberPoliticalAffiliationMapper {

    public MemberPoliticalAffiliationResponse toResponse(
            MemberPoliticalAffiliation affiliation
    ) {
        return new MemberPoliticalAffiliationResponse(
                affiliation.getId(),

                affiliation.getMember() == null
                        ? null
                        : affiliation.getMember().getId(),

                affiliation.getAffiliationName(),
                affiliation.getPositionTitle(),
                affiliation.getLocation(),
                affiliation.getStartDate(),
                affiliation.getEndDate(),
                affiliation.getCreatedAt(),
                affiliation.getUpdatedAt()
        );
    }

    public void updateEntity(
            MemberPoliticalAffiliation affiliation,
            MemberPoliticalAffiliationRequest request
    ) {
        affiliation.setAffiliationName(
                normalizeRequired(request.affiliationName())
        );

        affiliation.setPositionTitle(
                normalizeOptional(request.positionTitle())
        );

        affiliation.setLocation(
                normalizeOptional(request.location())
        );

        affiliation.setStartDate(
                request.startDate()
        );

        affiliation.setEndDate(
                request.endDate()
        );
    }

    private String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Affiliation name is required"
            );
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}