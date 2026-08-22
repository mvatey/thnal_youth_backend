package org.example.tnal_youth_backend.activity.service;

import lombok.RequiredArgsConstructor;

import org.example.tnal_youth_backend.activity.mapper.ActivityParticipantMapper;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.activity.model.enums.ParticipantRegistrationSource;
import org.example.tnal_youth_backend.activity.model.response.ActivityParticipantResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityParticipantSummaryResponse;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityParticipantViewService {

    private final StaffBranchScopeService staffBranchScopeService;

    private final ActivityRepository
            activityRepository;

    private final ActivityParticipantRepository
            participantRepository;

    private final ActivityInvitedBranchRepository
            invitedBranchRepository;

    private final UserRepository
            userRepository;

    private final MemberRepository
            memberRepository;

    private final BranchStaffRepository
            branchStaffRepository;

    private final ActivityParticipantMapper
            participantMapper;

    /**
     * Participant LIST.
     *
     * Host staff:
     * → host participant records only.
     *
     * Invited staff:
     * → own invited branch records only.
     *
     * Admin/viewer:
     * → all records.
     */
    @Transactional(readOnly = true)
    public List<ActivityParticipantResponse>
    getParticipants(
            Long activityId,
            Long currentUserId,
            Long selectedBranchId
    ) {

        Activity activity =
                findActivity(
                        activityId
                );

        User currentUser =
                findUser(
                        currentUserId
                );

        if (
                currentUser.getRole()
                        == UserRole.MEMBER
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Members cannot view "
                            + "the participant list"
            );
        }

        List<ActivityParticipant>
                allParticipants =
                participantRepository
                        .findAllByActivity_IdOrderByRegisteredAtDesc(
                                activityId
                        );

        /*
         * Admin / viewer = read all.
         */
        if (
                currentUser.getRole()
                        == UserRole.ADMIN
                        ||
                        currentUser.getRole()
                                == UserRole.VIEWER
        ) {

            return allParticipants
                    .stream()
                    .map(
                            participantMapper
                                    ::toResponse
                    )
                    .toList();
        }

        Long scopedBranchId =
                resolveScopedBranchId(
                        activity,
                        currentUser,
                        selectedBranchId
                );

        return allParticipants
                .stream()
                .filter(
                        participant -> {

                            Member member =
                                    participant
                                            .getMember();

                            return member
                                    != null
                                    &&
                                    scopedBranchId
                                            .equals(
                                                    member.getBranchId()
                                            );
                        }
                )
                .map(
                        participantMapper
                                ::toResponse
                )
                .toList();
    }

    /**
     * GLOBAL SUMMARY.
     *
     * Used by host page for 4 cards:
     *
     * total
     * attended
     * not attended
     * invited branch participants
     */
    @Transactional(readOnly = true)
    public ActivityParticipantSummaryResponse
    getSummary(
            Long activityId,
            Long currentUserId,
            Long selectedBranchId
    ) {

        Activity activity =
                findActivity(
                        activityId
                );

        User currentUser =
                findUser(
                        currentUserId
                );

        if (
                currentUser.getRole()
                        == UserRole.MEMBER
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Members cannot view "
                            + "participant summary"
            );
        }

        /*
         * Verify staff has access.
         */
        if (
                currentUser.getRole()
                        != UserRole.ADMIN
                        &&
                        currentUser.getRole()
                                != UserRole.VIEWER
        ) {

            resolveScopedBranchId(
                    activity,
                    currentUser,
                    selectedBranchId
            );
        }

        List<ActivityParticipant>
                participants =
                participantRepository
                        .findAllByActivity_IdOrderByRegisteredAtDesc(
                                activityId
                        );

        long total =
                participants.size();

        long attended =
                participants
                        .stream()
                        .filter(
                                this::isPresent
                        )
                        .count();

        long notAttended =
                Math.max(
                        0,
                        total - attended
                );

        long invitedBranchParticipants =
                participants
                        .stream()
                        .filter(
                                participant ->
                                        participant
                                                .getRegistrationSource()
                                                ==
                                                ParticipantRegistrationSource
                                                        .INVITED_BRANCH
                        )
                        .count();

        return ActivityParticipantSummaryResponse
                .builder()
                .total(total)
                .attended(attended)
                .notAttended(
                        notAttended
                )
                .invitedBranchParticipants(
                        invitedBranchParticipants
                )
                .build();
    }

    private boolean isPresent(
            ActivityParticipant participant
    ) {

        if (
                participant
                        .getAttendanceStatus()
                        != null
                        &&
                        participant
                                .getAttendanceStatus()
                                .getCode()
                                != null
        ) {

            return "PRESENT"
                    .equalsIgnoreCase(
                            participant
                                    .getAttendanceStatus()
                                    .getCode()
                    );
        }

        return participant
                .getCheckedInAt()
                != null;
    }

    private Long resolveScopedBranchId(
            Activity activity,
            User currentUser,
            Long selectedBranchId
    ) {

        if (
                currentUser.getRole()
                        != UserRole.BRANCH_LEADER
                        &&
                        currentUser.getRole()
                                != UserRole.SECRETARY
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only activity staff can "
                            + "view participants"
            );
        }

        Set<Long> staffBranchIds =
                resolveStaffBranchIds(
                        currentUser
                );

        Long hostBranchId =
                activity.getBranchId();

        // Prefer the branch selected in the sidebar. Without this check a
        // multi-branch secretary can be host staff in one branch and invited
        // staff in another, but the host branch would always win.
        if (selectedBranchId != null) {
            if (!staffBranchIds.contains(selectedBranchId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You do not have access to the selected branch"
                );
            }

            if (hostBranchId != null && hostBranchId.equals(selectedBranchId)) {
                return selectedBranchId;
            }

            boolean accepted = invitedBranchRepository
                    .findByActivity_IdAndBranch_IdAndInvitationStatus(
                            activity.getId(),
                            selectedBranchId,
                            ActivityInvitationStatus.ACCEPTED
                    )
                    .isPresent();

            if (accepted) {
                return selectedBranchId;
            }

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only view participants for your selected activity branch"
            );
        }

        /*
         * Host staff.
         */
        if (
                hostBranchId != null
                        &&
                        staffBranchIds.contains(
                                hostBranchId
                        )
        ) {

            return hostBranchId;
        }

        /*
         * Accepted invited branch.
         */
        for (
                Long staffBranchId
                : staffBranchIds
        ) {

            boolean accepted =
                    invitedBranchRepository
                            .findByActivity_IdAndBranch_IdAndInvitationStatus(
                                    activity.getId(),
                                    staffBranchId,
                                    ActivityInvitationStatus
                                            .ACCEPTED
                            )
                            .isPresent();

            if (accepted) {
                return staffBranchId;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only view participants "
                        + "for your own host branch "
                        + "or your accepted invited branch"
        );
    }

    private Set<Long>
    resolveStaffBranchIds(
            User user
    ) {
        return staffBranchScopeService.staffBranchIds(user);
    }

    private Activity findActivity(
            Long activityId
    ) {

        if (
                activityId == null
        ) {

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

    private User findUser(
            Long currentUserId
    ) {

        if (
                currentUserId == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return userRepository
                .findById(
                        currentUserId
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found"
                                )
                );
    }
}