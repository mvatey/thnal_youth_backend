package org.example.tnal_youth_backend.member.politicalaffiliation.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MemberPoliticalAffiliationResponse(

        Long id,

        Long memberId,

        String affiliationName,

        String positionTitle,

        String location,

        LocalDate startDate,

        LocalDate endDate,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}