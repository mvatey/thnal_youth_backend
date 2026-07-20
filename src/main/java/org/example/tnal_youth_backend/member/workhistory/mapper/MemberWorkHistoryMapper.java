package org.example.tnal_youth_backend.member.workhistory.mapper;

import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;
import org.example.tnal_youth_backend.member.workhistory.entity.MemberWorkHistory;
import org.springframework.stereotype.Component;

@Component
public class MemberWorkHistoryMapper {

    public MemberWorkHistoryResponse toResponse(
            MemberWorkHistory workHistory
    ) {
        if (workHistory == null) {
            return null;
        }

        return new MemberWorkHistoryResponse(
                workHistory.getId(),
                workHistory.getMember().getId(),
                workHistory.getOrganizationName(),
                workHistory.getPositionTitle(),
                workHistory.getAddress(),
                workHistory.getEmploymentSectorId(),
                workHistory.getStartDate(),
                workHistory.getEndDate(),
                workHistory.getCreatedAt(),
                workHistory.getUpdatedAt()
        );
    }
}