package org.example.tnal_youth_backend.member.participation.mapper;

import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationResponse;
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

                participant.getActivity() != null
                        ? participant.getActivity().getId()
                        : null,

                participant.getMember() != null
                        ? participant.getMember().getId()
                        : null,

                participant.getAttendanceStatusId(),

                participant.getRegisteredAt(),

                participant.getCheckedInAt(),

                participant.getInvitedBy() != null
                        ? participant.getInvitedBy().getId()
                        : null,

                participant.getNote(),

                participant.getCreatedAt(),

                participant.getUpdatedAt()
        );
    }
}