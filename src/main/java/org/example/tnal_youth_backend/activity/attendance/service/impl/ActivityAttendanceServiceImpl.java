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
import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.activity.repository.ActivityParticipantRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityAttendanceServiceImpl
        implements ActivityAttendanceService {

    private static final String PRESENT = "PRESENT";
    private static final String ABSENT = "ABSENT";

    private final ActivityRepository activityRepository;

    private final ActivityParticipantRepository participantRepository;

    private final AttendanceStatusRepository attendanceStatusRepository;

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ActivityAttendancePageResponse getAttendance(
            Long activityId
    ) {
        findActivity(activityId);

        List<ActivityParticipant> participants =
                participantRepository
                        .findAllByActivity_IdOrderByRegisteredAtDesc(
                                activityId
                        );

        Map<Short, String> statusCodes =
                loadAttendanceStatusCodes();

        List<ActivityAttendanceResponse> attendance =
                participants.stream()
                        .map(participant ->
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

        return ActivityAttendancePageResponse.builder()
                .attendance(attendance)
                .summary(summary)
                .build();
    }

    @Override
    @Transactional
    public ActivityAttendanceResponse checkIn(
            Long activityId,
            AttendanceMemberRequest request,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        validateAttendanceCanBeModified(activity);
        validateCurrentUser(currentUserId);

        ActivityParticipant participant =
                findParticipant(
                        activityId,
                        request.getMemberId()
                );

        if (participant.getCheckedInAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participant has already checked in"
            );
        }

        AttendanceStatus presentStatus =
                findAttendanceStatus(PRESENT);

        participant.setAttendanceStatusId(
                presentStatus.getId()
        );

        participant.setCheckedInAt(
                OffsetDateTime.now()
        );

        /*
         * Reset checkout in case an incorrect attendance
         * state existed before check-in.
         */
        participant.setCheckedOutAt(null);

        ActivityParticipant savedParticipant =
                participantRepository.saveAndFlush(
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
        Activity activity = findActivity(activityId);

        validateAttendanceCanBeModified(activity);
        validateCurrentUser(currentUserId);

        ActivityParticipant participant =
                findParticipant(
                        activityId,
                        request.getMemberId()
                );

        if (participant.getCheckedInAt() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Participant must check in before checking out"
            );
        }

        if (participant.getCheckedOutAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participant has already checked out"
            );
        }

        participant.setCheckedOutAt(
                OffsetDateTime.now()
        );

        ActivityParticipant savedParticipant =
                participantRepository.saveAndFlush(
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
        Activity activity = findActivity(activityId);

        validateManualAttendanceCanBeModified(activity);
        validateCurrentUser(currentUserId);

        ActivityParticipant participant =
                findParticipant(
                        activityId,
                        request.getMemberId()
                );

        String statusCode =
                normalizeStatusCode(
                        request.getAttendanceStatus()
                );

        if (!PRESENT.equals(statusCode)
                && !ABSENT.equals(statusCode)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance status must be PRESENT or ABSENT"
            );
        }

        AttendanceStatus attendanceStatus =
                findAttendanceStatus(statusCode);

        participant.setAttendanceStatusId(
                attendanceStatus.getId()
        );

        switch (statusCode) {
            case PRESENT -> {
                if (participant.getCheckedInAt() == null) {
                    participant.setCheckedInAt(
                            OffsetDateTime.now()
                    );
                }

                participant.setCheckedOutAt(null);
            }

            case ABSENT -> {
                participant.setCheckedInAt(null);
                participant.setCheckedOutAt(null);
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported attendance status"
            );
        }

        ActivityParticipant savedParticipant =
                participantRepository.saveAndFlush(
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

        return activityRepository.findById(activityId)
                .orElseThrow(() ->
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
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member is not a registered participant "
                                        + "of this activity"
                        )
                );
    }

    private AttendanceStatus findAttendanceStatus(
            String code
    ) {
        return attendanceStatusRepository
                .findByCodeIgnoreCase(code)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Attendance status was not found: "
                                        + code
                        )
                );
    }

    private void validateCurrentUser(
            Long currentUserId
    ) {
        if (currentUserId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user ID is required"
            );
        }

        if (!userRepository.existsById(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
            );
        }
    }

    private void validateAttendanceCanBeModified(
            Activity activity
    ) {
        if (activity.getStatus() == null
                || activity.getStatus().getCode() == null) {
            return;
        }

        String statusCode =
                activity.getStatus()
                        .getCode()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if ("COMPLETED".equals(statusCode)
                || "CANCELLED".equals(statusCode)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attendance cannot be modified for a "
                            + "completed or cancelled activity"
            );
        }
    }

    /**
     * Manual attendance correction is part of the completed-activity flow.
     * A completed activity may therefore still be marked present/absent, while
     * a cancelled activity remains locked. Check-in and check-out continue to
     * use the stricter validation above.
     */
    private void validateManualAttendanceCanBeModified(
            Activity activity
    ) {
        if (activity.getStatus() == null
                || activity.getStatus().getCode() == null) {
            return;
        }

        String statusCode =
                activity.getStatus()
                        .getCode()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if ("CANCELLED".equals(statusCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attendance cannot be modified for a cancelled activity"
            );
        }
    }

    private String normalizeStatusCode(
            String value
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Attendance status is required"
            );
        }

        return value.trim()
                .toUpperCase(Locale.ROOT);
    }

    private Map<Short, String>
    loadAttendanceStatusCodes() {

        Map<Short, String> statusCodes =
                new HashMap<>();

        for (AttendanceStatus status
                : attendanceStatusRepository.findAll()) {

            if (status.getId() != null
                    && status.getCode() != null) {

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
        Member member = participant.getMember();

        String attendanceStatusCode = null;

        if (participant.getAttendanceStatusId() != null) {
            attendanceStatusCode =
                    statusCodes.get(
                            participant.getAttendanceStatusId()
                    );
        }

        return ActivityAttendanceResponse.builder()
                .participantId(participant.getId())
                .activityId(
                        participant.getActivity() != null
                                ? participant.getActivity().getId()
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
                        participant.getAttendanceStatusId()
                )
                .attendanceStatus(
                        attendanceStatusCode
                )
                .checkedInAt(
                        participant.getCheckedInAt()
                )
                .checkedOutAt(
                        participant.getCheckedOutAt()
                )
                .registeredAt(
                        participant.getRegisteredAt()
                )
                .build();
    }

    private ActivityAttendanceSummaryResponse buildSummary(
            List<ActivityParticipant> participants,
            Map<Short, String> statusCodes
    ) {
        long present = 0;
        long absent = 0;
        long checkedIn = 0;
        long checkedOut = 0;
        long notRecorded = 0;

        for (ActivityParticipant participant : participants) {

            if (participant.getCheckedInAt() != null) {
                checkedIn++;
            }

            if (participant.getCheckedOutAt() != null) {
                checkedOut++;
            }

            Short statusId =
                    participant.getAttendanceStatusId();

            if (statusId == null) {
                notRecorded++;
                continue;
            }

            String statusCode =
                    statusCodes.get(statusId);

            if (statusCode == null) {
                notRecorded++;
                continue;
            }

            switch (
                    statusCode.trim()
                            .toUpperCase(Locale.ROOT)
            ) {
                case PRESENT -> present++;
                case ABSENT -> absent++;
                default -> notRecorded++;
            }
        }

        return ActivityAttendanceSummaryResponse.builder()
                .totalParticipants(participants.size())
                .present(present)
                .absent(absent)
                .checkedIn(checkedIn)
                .checkedOut(checkedOut)
                .notRecorded(notRecorded)
                .build();
    }
}