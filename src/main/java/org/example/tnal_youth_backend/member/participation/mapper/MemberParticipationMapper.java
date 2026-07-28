package org.example.tnal_youth_backend.member.participation.mapper;

import org.example.tnal_youth_backend.activity.attendance.entity.AttendanceStatus;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.activity.model.entity.ActivitySector;
import org.example.tnal_youth_backend.activity.model.entity.ActivityType;
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

        Activity activity =
                participant.getActivity();

        return new MemberParticipationResponse(
                participant.getId(),

                activity != null
                        ? activity.getId()
                        : null,

                participant.getMember() != null
                        ? participant.getMember().getId()
                        : null,

                activity != null
                        ? activity.getTitleKm()
                        : null,

                activity != null
                        ? activity.getTitleEn()
                        : null,

                activity != null
                        ? toSectorResponse(
                        activity.getSector()
                )
                        : null,

                activity != null
                        ? toTypeResponse(
                        activity.getType()
                )
                        : null,

                toAttendanceStatusResponse(
                        participant.getAttendanceStatus()
                ),

                activity != null
                        ? new MemberParticipationResponse
                        .LocationResponse(
                        activity.getLocationName(),
                        activity.getAddress()
                )
                        : null,

                activity != null
                        ? activity.getStartsAt()
                        : null,

                activity != null
                        ? activity.getEndsAt()
                        : null,

                participant.getRegisteredAt(),

                participant.getCheckedInAt(),

                participant.getCheckedOutAt(),

                participant.getInvitedBy() != null
                        ? participant.getInvitedBy().getId()
                        : null,

                participant.getNote()
        );
    }

    private MemberParticipationResponse.SectorResponse
    toSectorResponse(
            ActivitySector sector
    ) {
        if (sector == null) {
            return null;
        }

        return new MemberParticipationResponse
                .SectorResponse(
                sector.getId(),
                sector.getCode(),
                sector.getLabelKm(),
                sector.getLabelEn()
        );
    }

    private MemberParticipationResponse.TypeResponse
    toTypeResponse(
            ActivityType type
    ) {
        if (type == null) {
            return null;
        }

        return new MemberParticipationResponse
                .TypeResponse(
                type.getId(),
                type.getCode(),
                type.getLabelKm(),
                type.getLabelEn()
        );
    }

    private MemberParticipationResponse
            .AttendanceStatusResponse
    toAttendanceStatusResponse(
            AttendanceStatus status
    ) {
        if (status == null) {
            return null;
        }

        return new MemberParticipationResponse
                .AttendanceStatusResponse(
                status.getId(),
                status.getCode(),
                status.getLabelKm(),
                status.getLabelEn()
        );
    }
}