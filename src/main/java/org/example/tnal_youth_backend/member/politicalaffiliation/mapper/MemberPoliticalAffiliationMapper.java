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
        if (affiliation == null) {
            return null;
        }

        return new MemberPoliticalAffiliationResponse(
                affiliation.getId(),

                affiliation.getMember() == null
                        ? null
                        : affiliation.getMember().getId(),

                affiliation.getPartyId(),
                affiliation.getCountry(),
                affiliation.getLocation(),
                affiliation.getPositionTitle(),
                affiliation.getCardNo(),
                affiliation.getStartDate(),
                affiliation.getEndDate(),
                affiliation.getIsCurrent(),
                affiliation.getNote(),
                affiliation.getCreatedAt(),
                affiliation.getUpdatedAt()
        );
    }

    public void updateEntity(
            MemberPoliticalAffiliation affiliation,
            MemberPoliticalAffiliationRequest request
    ) {
        affiliation.setPartyId(
                request.partyId()
        );

        affiliation.setCountry(
                trimToNull(
                        request.country()
                )
        );

        affiliation.setLocation(
                trimToNull(
                        request.location()
                )
        );

        affiliation.setPositionTitle(
                trimToNull(
                        request.positionTitle()
                )
        );

        affiliation.setCardNo(
                trimToNull(
                        request.cardNo()
                )
        );

        affiliation.setStartDate(
                request.startDate()
        );

        affiliation.setEndDate(
                request.endDate()
        );

        affiliation.setIsCurrent(
                Boolean.TRUE.equals(
                        request.isCurrent()
                )
        );

        affiliation.setNote(
                trimToNull(
                        request.note()
                )
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


    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}