package org.example.tnal_youth_backend.member.participation.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.participation.dto.request.MemberParticipationRequest;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationResponse;
import org.example.tnal_youth_backend.member.participation.entity.ActivityParticipant;
import org.example.tnal_youth_backend.member.participation.mapper.MemberParticipationMapper;
import org.example.tnal_youth_backend.member.participation.repository.ActivityParticipantRepository;
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

    private final MemberRepository memberRepository;

    private final MemberParticipationMapper
            memberParticipationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MemberParticipationResponse>
    getParticipationsByMemberId(Long memberId) {

        verifyMemberExists(memberId);

        return activityParticipantRepository
                .findAllByMemberIdOrderByRegisteredAtDescIdDesc(
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
        Member member = findMember(memberId);

        if (activityParticipantRepository
                .existsByActivityIdAndMemberId(
                        request.activityId(),
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

        ActivityParticipant participant =
                ActivityParticipant.builder()
                        .activityId(request.activityId())
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
                        .invitedById(
                                request.invitedById()
                        )
                        .note(trimToNull(request.note()))
                        .build();

        try {
            ActivityParticipant saved =
                    activityParticipantRepository
                            .saveAndFlush(participant);

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

        boolean duplicate =
                activityParticipantRepository
                        .existsByActivityIdAndMemberIdAndIdNot(
                                request.activityId(),
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

        participant.setActivityId(
                request.activityId()
        );

        participant.setAttendanceStatusId(
                request.attendanceStatusId()
        );

        participant.setRegisteredAt(
                request.registeredAt()
        );

        participant.setCheckedInAt(
                request.checkedInAt()
        );

        participant.setInvitedById(
                request.invitedById()
        );

        participant.setNote(
                trimToNull(request.note())
        );

        try {
            ActivityParticipant updated =
                    activityParticipantRepository
                            .saveAndFlush(participant);

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

        activityParticipantRepository.delete(participant);
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    private void verifyMemberExists(Long memberId) {
        findMember(memberId);
    }

    private ActivityParticipant findParticipation(
            Long memberId,
            Long participationId
    ) {
        return activityParticipantRepository
                .findByIdAndMemberId(
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
                && checkedInAt.isBefore(registeredAt)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Checked-in time cannot be before registered time"
            );
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

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