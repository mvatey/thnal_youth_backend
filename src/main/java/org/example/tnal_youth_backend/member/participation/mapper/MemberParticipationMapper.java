package org.example.tnal_youth_backend.member.participation.mapper;

import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationResponse;
import org.example.tnal_youth_backend.member.participation.entity.ActivityParticipant;
import org.springframework.stereotype.Component;

@Component
public class MemberParticipationMapper {

    public MemberParticipationResponse toResponse(
            ActivityParticipant participant
    ) {
        if (participant == null) {
            return null;
        }

        return new MemberParticipationResponse(
                participant.getId(),
                participant.getActivityId(),
                participant.getMember().getId(),
                participant.getAttendanceStatusId(),
                participant.getRegisteredAt(),
                participant.getCheckedInAt(),
                participant.getInvitedById(),
                participant.getNote(),
                participant.getCreatedAt(),
                participant.getUpdatedAt()
        );
    }
}