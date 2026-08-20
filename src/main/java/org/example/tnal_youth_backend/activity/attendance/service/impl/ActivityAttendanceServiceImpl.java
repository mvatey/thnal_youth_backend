package org.example.tnal_youth_backend.activity.attendance.service.impl;

import lombok.RequiredArgsConstructor;

import org.example.tnal_youth_backend.activity.attendance.dto.request.AttendanceMemberRequest;
import org.example.tnal_youth_backend.activity.attendance.dto.request.UpdateAttendanceStatusRequest;
import org.example.tnal_youth_backend.activity.attendance.dto.response.ActivityAttendancePageResponse;
import org.example.tnal_youth_backend.activity.attendance.dto.response.ActivityAttendanceResponse;
import org.example.tnal_youth_backend.activity.attendance.dto.response.ActivityAttendanceSummaryResponse;

import org.example.tnal_youth_backend.activity.attendance.entity.AttendanceStatus;

import org.example.tnal_youth_backend.activity.attendance.repository.AttendanceStatusRepository;

import org.example.tnal_youth_backend.activity.attendance.service.ActivityAttendanceService;

import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivityInvitedBranch;
import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;

import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.activity.model.enums.ParticipantRegistrationSource;

import org.example.tnal_youth_backend.activity.repository.ActivityInvitedBranchRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityParticipantRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;

import org.example.tnal_youth_backend.authentication.repository.UserRepository;

import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;

import org.example.tnal_youth_backend.member.member.entity.Member;

import org.example.tnal_youth_backend.member.member.repository.MemberRepository;

import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityAttendanceServiceImpl
        implements ActivityAttendanceService {

    private final StaffBranchScopeService staffBranchScopeService;

    private static final String PRESENT =
            "PRESENT";

    private static final String ABSENT =
            "ABSENT";

    private final ActivityRepository
            activityRepository;

    private final ActivityParticipantRepository
            participantRepository;

    private final AttendanceStatusRepository
            attendanceStatusRepository;

    private final UserRepository
            userRepository;

    private final MemberRepository
            memberRepository;

    private final BranchStaffRepository
            branchStaffRepository;

    private final ActivityInvitedBranchRepository
            invitedBranchRepository;

    @Override
    @Transactional(readOnly = true)
    public ActivityAttendancePageResponse getAttendance(
            Long activityId
    ) {

        findActivity(
                activityId
        );

        List<ActivityParticipant> participants =
                participantRepository
                        .findAllByActivity_IdOrderByRegisteredAtDesc(
                                activityId
                        );

        Map<Short, String> statusCodes =
                loadAttendanceStatusCodes();

        List<ActivityAttendanceResponse> attendance =
                participants
                        .stream()
                        .map(
                                participant ->
                                        toResponse(
                                                participant,
                                                statusCodes
                                        )
                        )
                        .toList();

        ActivityAttendanceSummaryResponse summary =
                buildSummary(
                        participants,
                        statusCodes
                );

        return ActivityAttendancePageResponse
                .builder()
                .attendance(
                        attendance
                )
                .summary(
                        summary
                )
                .build();
    }

    @Override
    @Transactional
    public ActivityAttendanceResponse checkIn(
            Long activityId,
            AttendanceMemberRequest request,
            Long currentUserId
    ) {

        Activity activity =
                findActivity(
                        activityId
                );

        validateAttendanceCanBeModified(
                activity
        );

        ActivityParticipant participant =
                findParticipant(
                        activityId,
                        request.getMemberId()
                );

        /*
         * IMPORTANT:
         *
         * Check-in must use the same
         * branch permission as manual
         * attendance.
         */
        Member member =
                participant.getMember();

        if (member == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Participant is not linked to a member"
            );
        }

        validateManualAttendancePermission(
                activity,
                member.getBranchId(),
                currentUserId
        );

        if (
                participant.getCheckedInAt()
                        != null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participant has already checked in"
            );
        }

        AttendanceStatus presentStatus =
                findAttendanceStatus(
                        PRESENT
                );

        participant.setAttendanceStatusId(
                presentStatus.getId()
        );

        OffsetDateTime checkedInAt =
                OffsetDateTime.now();

        if (
                participant.getRegisteredAt()
                        != null
                        &&
                        checkedInAt.isBefore(
                                participant
                                        .getRegisteredAt()
                        )
        ) {

            checkedInAt =
                    participant
                            .getRegisteredAt();
        }

        participant.setCheckedInAt(
                checkedInAt
        );

        participant.setCheckedOutAt(
                null
        );

        ActivityParticipant savedParticipant =
                participantRepository
                        .saveAndFlush(
                                participant
                        );

        return toResponse(
                savedParticipant,
                Map.of(
                        presentStatus.getId(),
                        presentStatus.getCode()
                )
        );
    }

    @Override
    @Transactional
    public ActivityAttendanceResponse checkOut(
            Long activityId,
            AttendanceMemberRequest request,
            Long currentUserId
    ) {

        Activity activity =
                findActivity(
                        activityId
                );

        validateAttendanceCanBeModified(
                activity
        );

        ActivityParticipant participant =
                findParticipant(
                        activityId,
                        request.getMemberId()
                );

        Member member =
                participant.getMember();

        if (member == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Participant is not linked to a member"
            );
        }

        /*
         * Same branch restriction as
         * check-in/manual status.
         */
        validateManualAttendancePermission(
                activity,
                member.getBranchId(),
                currentUserId
        );

        if (
                participant.getCheckedInAt()
                        == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Participant must check in before checking out"
            );
        }

        if (
                participant.getCheckedOutAt()
                        != null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participant has already checked out"
            );
        }

        participant.setCheckedOutAt(
                OffsetDateTime.now()
        );

        ActivityParticipant savedParticipant =
                participantRepository
                        .saveAndFlush(
                                participant
                        );

        return toResponse(
                savedParticipant,
                loadAttendanceStatusCodes()
        );
    }

    @Override
    @Transactional
    public ActivityAttendanceResponse updateStatus(
            Long activityId,
            UpdateAttendanceStatusRequest request,
            Long currentUserId
    ) {

        Activity activity =
                findActivity(
                        activityId
                );

        /*
         * Manual attendance is allowed
         * before/during/after Activity.
         *
         * CANCELLED stays locked.
         */
        validateManualAttendanceCanBeModified(
                activity
        );

        Long memberId =
                request.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        Member member =
                memberRepository
                        .findById(
                                memberId
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Member not found with ID: "
                                                        + memberId
                                        )
                        );

        /*
         * Host staff:
         * → host members only.
         *
         * Invited staff:
         * → own accepted branch members only.
         */
        validateManualAttendancePermission(
                activity,
                member.getBranchId(),
                currentUserId
        );

        /*
         * No participant yet?
         *
         * Create WALK_IN / INVITED_BRANCH
         * participant automatically.
         */
        ActivityParticipant participant =
                findOrCreateParticipant(
                        activity,
                        member,
                        currentUserId
                );

        String statusCode =
                normalizeStatusCode(
                        request
                                .getAttendanceStatus()
                );

        if (
                !PRESENT.equals(
                        statusCode
                )
                        &&
                        !ABSENT.equals(
                                statusCode
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance status must be PRESENT or ABSENT"
            );
        }

        AttendanceStatus attendanceStatus =
                findAttendanceStatus(
                        statusCode
                );

        participant.setAttendanceStatusId(
                attendanceStatus.getId()
        );

        switch (statusCode) {

            case PRESENT -> {

                if (
                        participant
                                .getCheckedInAt()
                                == null
                ) {

                    OffsetDateTime checkedInAt =
                            OffsetDateTime.now();

                    if (
                            participant
                                    .getRegisteredAt()
                                    != null
                                    &&
                                    checkedInAt.isBefore(
                                            participant
                                                    .getRegisteredAt()
                                    )
                    ) {

                        checkedInAt =
                                participant
                                        .getRegisteredAt();
                    }

                    participant.setCheckedInAt(
                            checkedInAt
                    );
                }

                participant.setCheckedOutAt(
                        null
                );
            }

            case ABSENT -> {

                participant.setCheckedInAt(
                        null
                );

                participant.setCheckedOutAt(
                        null
                );
            }

            default ->
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unsupported attendance status"
                    );
        }

        ActivityParticipant savedParticipant =
                participantRepository
                        .saveAndFlush(
                                participant
                        );

        return toResponse(
                savedParticipant,
                Map.of(
                        attendanceStatus.getId(),
                        attendanceStatus.getCode()
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
                .findById(
                        activityId
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Activity not found with ID: "
                                                + activityId
                                )
                );
    }

    private ActivityParticipant findParticipant(
            Long activityId,
            Long memberId
    ) {

        if (memberId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        return participantRepository
                .findByActivity_IdAndMember_Id(
                        activityId,
                        memberId
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Member is not a registered participant "
                                                + "of this activity"
                                )
                );
    }

    private ActivityParticipant findOrCreateParticipant(
            Activity activity,
            Member member,
            Long currentUserId
    ) {

        return participantRepository
                .findByActivity_IdAndMember_Id(
                        activity.getId(),
                        member.getId()
                )
                .orElseGet(
                        () ->
                                createWalkInParticipant(
                                        activity,
                                        member,
                                        currentUserId
                                )
                );
    }

    private ActivityParticipant createWalkInParticipant(
            Activity activity,
            Member member,
            Long currentUserId
    ) {

        User actingUser =
                requireUser(
                        currentUserId
                );

        Long memberBranchId =
                member.getBranchId();

        if (memberBranchId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member "
                            + member.getId()
                            + " does not belong to a branch"
            );
        }

        /*
         * Defense in depth.
         *
         * Permission is already checked
         * before this method, but check
         * invited branch relationship again.
         */
        Long hostBranchId =
                activity.getBranchId();

        ParticipantRegistrationSource source;

        ActivityInvitedBranch invitedBranch =
                null;

        if (
                hostBranchId != null
                        &&
                        hostBranchId.equals(
                                memberBranchId
                        )
        ) {

            source =
                    ParticipantRegistrationSource
                            .WALK_IN;

        } else {

            invitedBranch =
                    invitedBranchRepository
                            .findByActivity_IdAndBranch_IdAndInvitationStatus(
                                    activity.getId(),
                                    memberBranchId,
                                    ActivityInvitationStatus
                                            .ACCEPTED
                            )
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.FORBIDDEN,
                                                    "The member's branch has not "
                                                            + "accepted an invitation "
                                                            + "for this activity"
                                            )
                            );

            source =
                    ParticipantRegistrationSource
                            .INVITED_BRANCH;
        }

        ActivityParticipant participant =
                ActivityParticipant
                        .builder()
                        .activity(
                                activity
                        )
                        .member(
                                member
                        )
                        .invitedBy(
                                actingUser
                        )
                        .invitedBranch(
                                invitedBranch
                        )
                        .registrationSource(
                                source
                        )
                        .registeredAt(
                                OffsetDateTime.now()
                        )
                        .build();

        return participantRepository
                .saveAndFlush(
                        participant
                );
    }

    private AttendanceStatus findAttendanceStatus(
            String code
    ) {

        return attendanceStatusRepository
                .findByCodeIgnoreCase(
                        code
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Attendance status was not found: "
                                                + code
                                )
                );
    }

    /**
     * Check-in / check-out:
     *
     * Allowed while Activity is active.
     *
     * COMPLETED or CANCELLED:
     * locked for check-in/out.
     *
     * Manual PRESENT/ABSENT corrections
     * use a different rule below.
     */
    private void validateAttendanceCanBeModified(
            Activity activity
    ) {

        if (
                activity.getStatus() == null
                        ||
                        activity
                                .getStatus()
                                .getCode()
                                == null
        ) {

            return;
        }

        String statusCode =
                activity
                        .getStatus()
                        .getCode()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                "COMPLETED"
                        .equals(
                                statusCode
                        )
                        ||
                        "CANCELLED"
                                .equals(
                                        statusCode
                                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Check-in/check-out cannot be modified "
                            + "for a completed or cancelled activity"
            );
        }
    }

    /**
     * Manual PRESENT / ABSENT.
     *
     * IMPORTANT FINAL RULE:
     *
     * Host and accepted invited staff
     * may manually update attendance:
     *
     * - before Activity
     * - during Activity
     * - after Activity
     *
     * Only CANCELLED is locked.
     */
    private void validateManualAttendanceCanBeModified(
            Activity activity
    ) {

        String statusCode =
                activity.getStatus() != null
                        &&
                        activity
                                .getStatus()
                                .getCode()
                                != null

                        ? activity
                          .getStatus()
                          .getCode()
                          .trim()
                          .toUpperCase(
                                  Locale.ROOT
                          )

                        : null;

        if (
                "CANCELLED"
                        .equals(
                                statusCode
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attendance cannot be modified "
                            + "for a cancelled activity"
            );
        }
    }

    /**
     * Branch-scoped attendance permission.
     *
     * HOST BRANCH A:
     *
     * A Secretary / Leader
     * → A member ✅
     * → B member ❌
     *
     * ACCEPTED INVITED BRANCH B:
     *
     * B Secretary / Leader
     * → B member ✅
     * → A member ❌
     * → C member ❌
     *
     * ADMIN: organization-wide access.
     * MEMBER / VIEWER: cannot manually modify.
     */
    private void validateManualAttendancePermission(
            Activity activity,
            Long memberBranchId,
            Long currentUserId
    ) {

        User currentUser =
                requireUser(
                        currentUserId
                );

        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        if (
                currentUser.getRole()
                        != UserRole.BRANCH_LEADER
                        &&
                        currentUser.getRole()
                                != UserRole.SECRETARY
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only a branch leader or secretary "
                            + "can update attendance"
            );
        }

        if (
                memberBranchId == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The member does not belong to a branch"
            );
        }

        Set<Long> staffBranchIds =
                resolveStaffBranchIds(
                        currentUser
                );

        Long hostBranchId =
                activity.getBranchId();

        /*
         * CASE 1:
         * HOST branch.
         *
         * Staff belongs to host
         * AND
         * member belongs to host.
         */
        if (
                hostBranchId != null
                        &&
                        hostBranchId.equals(
                                memberBranchId
                        )
                        &&
                        staffBranchIds.contains(
                                hostBranchId
                        )
        ) {

            return;
        }

        /*
         * CASE 2:
         * ACCEPTED invited branch.
         *
         * Staff must belong to the same
         * branch as the member.
         *
         * That branch must have ACCEPTED
         * the Activity invitation.
         */
        if (
                staffBranchIds.contains(
                        memberBranchId
                )
        ) {

            boolean branchAccepted =
                    invitedBranchRepository
                            .findByActivity_IdAndBranch_IdAndInvitationStatus(
                                    activity.getId(),
                                    memberBranchId,
                                    ActivityInvitationStatus
                                            .ACCEPTED
                            )
                            .isPresent();

            if (branchAccepted) {
                return;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only update attendance "
                        + "for members of your own branch"
        );
    }

    private Set<Long> resolveStaffBranchIds(
            User user
    ) {
        return staffBranchScopeService.staffBranchIds(user);
    }

    private User requireUser(
            Long userId
    ) {

        if (userId == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return userRepository
                .findById(
                        userId
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user could not be found"
                                )
                );
    }

    private String normalizeStatusCode(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance status is required"
            );
        }

        return value
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private Map<Short, String>
    loadAttendanceStatusCodes() {

        Map<Short, String> statusCodes =
                new HashMap<>();

        for (
                AttendanceStatus status
                :
                attendanceStatusRepository
                        .findAll()
        ) {

            if (
                    status.getId() != null
                            &&
                            status.getCode() != null
            ) {

                statusCodes.put(
                        status.getId(),
                        status.getCode()
                );
            }
        }

        return statusCodes;
    }

    private ActivityAttendanceResponse toResponse(
            ActivityParticipant participant,
            Map<Short, String> statusCodes
    ) {

        Member member =
                participant
                        .getMember();

        String attendanceStatusCode =
                null;

        if (
                participant
                        .getAttendanceStatusId()
                        != null
        ) {

            attendanceStatusCode =
                    statusCodes.get(
                            participant
                                    .getAttendanceStatusId()
                    );
        }

        return ActivityAttendanceResponse
                .builder()
                .participantId(
                        participant.getId()
                )
                .activityId(
                        participant.getActivity()
                                != null
                                ? participant
                                  .getActivity()
                                  .getId()
                                : null
                )
                .memberId(
                        member != null
                                ? member.getId()
                                : null
                )
                .memberNo(
                        member != null
                                ? member.getMemberNo()
                                : null
                )
                .fullNameKm(
                        member != null
                                ? member.getFullNameKm()
                                : null
                )
                .fullNameEn(
                        member != null
                                ? member.getFullNameEn()
                                : null
                )
                .phone(
                        member != null
                                ? member.getPhone()
                                : null
                )
                .email(
                        member != null
                                ? member.getEmail()
                                : null
                )
                .branchId(
                        member != null
                                ? member.getBranchId()
                                : null
                )
                .attendanceStatusId(
                        participant
                                .getAttendanceStatusId()
                )
                .attendanceStatus(
                        attendanceStatusCode
                )
                .checkedInAt(
                        participant
                                .getCheckedInAt()
                )
                .checkedOutAt(
                        participant
                                .getCheckedOutAt()
                )
                .registeredAt(
                        participant
                                .getRegisteredAt()
                )
                .build();
    }

    private ActivityAttendanceSummaryResponse buildSummary(
            List<ActivityParticipant> participants,
            Map<Short, String> statusCodes
    ) {

        long present =
                0;

        long absent =
                0;

        long checkedIn =
                0;

        long checkedOut =
                0;

        long notRecorded =
                0;

        for (
                ActivityParticipant participant
                :
                participants
        ) {

            if (
                    participant
                            .getCheckedInAt()
                            != null
            ) {

                checkedIn++;
            }

            if (
                    participant
                            .getCheckedOutAt()
                            != null
            ) {

                checkedOut++;
            }

            Short statusId =
                    participant
                            .getAttendanceStatusId();

            if (
                    statusId == null
            ) {

                notRecorded++;
                continue;
            }

            String statusCode =
                    statusCodes.get(
                            statusId
                    );

            if (
                    statusCode == null
            ) {

                notRecorded++;
                continue;
            }

            switch (
                    statusCode
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            ) {

                case PRESENT ->
                        present++;

                case ABSENT ->
                        absent++;

                default ->
                        notRecorded++;
            }
        }

        return ActivityAttendanceSummaryResponse
                .builder()
                .totalParticipants(
                        participants.size()
                )
                .present(
                        present
                )
                .absent(
                        absent
                )
                .checkedIn(
                        checkedIn
                )
                .checkedOut(
                        checkedOut
                )
                .notRecorded(
                        notRecorded
                )
                .build();
    }
}
