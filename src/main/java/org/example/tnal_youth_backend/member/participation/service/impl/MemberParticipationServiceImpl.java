package org.example.tnal_youth_backend.member.participation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.activity.model.enums.ParticipantRegistrationSource;
import org.example.tnal_youth_backend.activity.repository.ActivityParticipantRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.participation.dto.request.MemberParticipationRequest;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationResponse;
import org.example.tnal_youth_backend.member.participation.mapper.MemberParticipationMapper;
import org.example.tnal_youth_backend.member.participation.service.MemberParticipationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberParticipationServiceImpl
        implements MemberParticipationService {

    private final ActivityParticipantRepository
            activityParticipantRepository;

    private final ActivityRepository
            activityRepository;

    private final MemberRepository
            memberRepository;

    private final UserRepository
            userRepository;

    private final MemberParticipationMapper
            memberParticipationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberParticipationResponse>
    getParticipationsByMemberId(
            Long memberId
    ) {
        verifyMemberExists(memberId);

        return activityParticipantRepository
                .findAllByMember_IdOrderByRegisteredAtDescIdDesc(
                        memberId
                )
                .stream()
                .map(memberParticipationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MemberParticipationResponse create(
            Long memberId,
            MemberParticipationRequest request
    ) {
        Member member =
                findMember(memberId);

        Activity activity =
                findActivity(
                        request.activityId()
                );

        if (activityParticipantRepository
                .existsByActivity_IdAndMember_Id(
                        activity.getId(),
                        memberId
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member already exists in this activity"
            );
        }

        validateCheckInTime(
                request.registeredAt(),
                request.checkedInAt()
        );

        User invitedBy =
                findInvitedByUser(
                        request.invitedById()
                );

        ActivityParticipant participant =
                ActivityParticipant.builder()
                        .activity(activity)
                        .member(member)
                        .attendanceStatusId(
                                request.attendanceStatusId()
                        )
                        .registeredAt(
                                request.registeredAt() != null
                                        ? request.registeredAt()
                                        : OffsetDateTime.now()
                        )
                        .checkedInAt(
                                request.checkedInAt()
                        )
                        .invitedBy(invitedBy)
                        .registrationSource(
                                ParticipantRegistrationSource.MANUAL
                        )
                        .note(
                                trimToNull(
                                        request.note()
                                )
                        )
                        .build();

        try {
            ActivityParticipant saved =
                    activityParticipantRepository
                            .saveAndFlush(
                                    participant
                            );

            return memberParticipationMapper
                    .toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException();
        }
    }

    @Override
    @Transactional
    public MemberParticipationResponse update(
            Long memberId,
            Long participationId,
            MemberParticipationRequest request
    ) {
        ActivityParticipant participant =
                findParticipation(
                        memberId,
                        participationId
                );

        Activity activity =
                findActivity(
                        request.activityId()
                );

        boolean duplicate =
                activityParticipantRepository
                        .existsByActivity_IdAndMember_IdAndIdNot(
                                activity.getId(),
                                memberId,
                                participationId
                        );

        if (duplicate) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member already exists in this activity"
            );
        }

        validateCheckInTime(
                request.registeredAt(),
                request.checkedInAt()
        );

        User invitedBy =
                findInvitedByUser(
                        request.invitedById()
                );

        participant.setActivity(activity);

        participant.setAttendanceStatusId(
                request.attendanceStatusId()
        );

        participant.setRegisteredAt(
                request.registeredAt() != null
                        ? request.registeredAt()
                        : participant.getRegisteredAt()
        );

        participant.setCheckedInAt(
                request.checkedInAt()
        );

        participant.setInvitedBy(invitedBy);

        participant.setNote(
                trimToNull(
                        request.note()
                )
        );

        try {
            ActivityParticipant updated =
                    activityParticipantRepository
                            .saveAndFlush(
                                    participant
                            );

            return memberParticipationMapper
                    .toResponse(updated);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException();
        }
    }

    @Override
    @Transactional
    public void delete(
            Long memberId,
            Long participationId
    ) {
        ActivityParticipant participant =
                findParticipation(
                        memberId,
                        participationId
                );

        activityParticipantRepository.delete(
                participant
        );
    }

    private Member findMember(
            Long memberId
    ) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        return memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    private Activity findActivity(
            Long activityId
    ) {
        if (activityId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity ID is required"
            );
        }

        return activityRepository
                .findById(activityId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Activity not found with ID: "
                                        + activityId
                        )
                );
    }

    private User findInvitedByUser(
            Long invitedById
    ) {
        if (invitedById == null) {
            return null;
        }

        return userRepository
                .findById(invitedById)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Inviting user not found with ID: "
                                        + invitedById
                        )
                );
    }

    private void verifyMemberExists(
            Long memberId
    ) {
        findMember(memberId);
    }

    private ActivityParticipant findParticipation(
            Long memberId,
            Long participationId
    ) {
        if (participationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Participation ID is required"
            );
        }

        return activityParticipantRepository
                .findByIdAndMember_Id(
                        participationId,
                        memberId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Participation not found with ID: "
                                        + participationId
                        )
                );
    }

    private void validateCheckInTime(
            OffsetDateTime registeredAt,
            OffsetDateTime checkedInAt
    ) {
        if (registeredAt != null
                && checkedInAt != null
                && checkedInAt.isBefore(
                registeredAt
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Checked-in time cannot be before registered time"
            );
        }
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

    private ResponseStatusException
    databaseConstraintException() {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Participation could not be saved. Check that \
                activity_id, attendance_status_id, and invited_by \
                reference existing records.
                """
        );
    }
}