package org.example.tnal_youth_backend.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.mapper.ActivityParticipantMapper;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivityInvitedBranch;
import org.example.tnal_youth_backend.activity.model.entity.ActivityParticipant;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.activity.model.enums.ParticipantRegistrationSource;
import org.example.tnal_youth_backend.activity.model.request.InviteParticipantsRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityParticipantResponse;
import org.example.tnal_youth_backend.activity.repository.ActivityInvitedBranchRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityParticipantRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.service.ActivityParticipantService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityParticipantServiceImpl
        implements ActivityParticipantService {

    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository
            participantRepository;

    private final ActivityInvitedBranchRepository
            invitedBranchRepository;

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    private final ActivityParticipantMapper participantMapper;

    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public List<ActivityParticipantResponse> inviteParticipants(
            Long activityId,
            InviteParticipantsRequest request,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);
        User invitedBy = findUser(currentUserId);

        validateActivityCanBeModified(activity);

        Set<Long> requestedMemberIds =
                new LinkedHashSet<>(
                        request.getMemberIds()
                );

        if (requestedMemberIds.contains(null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member IDs must not contain null"
            );
        }

        List<Member> members =
                memberRepository.findAllById(
                        requestedMemberIds
                );

        if (members.size() != requestedMemberIds.size()) {
            Set<Long> foundIds = new LinkedHashSet<>();

            for (Member member : members) {
                foundIds.add(member.getId());
            }

            Set<Long> missingIds =
                    new LinkedHashSet<>(
                            requestedMemberIds
                    );

            missingIds.removeAll(foundIds);

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Members not found: " + missingIds
            );
        }

        validateCapacity(
                activity,
                members.size()
        );

        List<ActivityParticipant> participants =
                new ArrayList<>();

        for (Member member : members) {
            if (participantRepository
                    .existsByActivity_IdAndMember_Id(
                            activityId,
                            member.getId()
                    )) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Member "
                                + member.getId()
                                + " is already a participant"
                );
            }

            ParticipantAccess participantAccess =
                    resolveParticipantAccess(
                            activity,
                            member
                    );

            ActivityParticipant participant =
                    ActivityParticipant.builder()
                            .activity(activity)
                            .member(member)
                            .invitedBy(invitedBy)
                            .invitedBranch(
                                    participantAccess
                                            .invitedBranch()
                            )
                            .registrationSource(
                                    participantAccess
                                            .registrationSource()
                            )
                            .registeredAt(
                                    OffsetDateTime.now()
                            )
                            .note(
                                    trimToNull(
                                            request.getNote()
                                    )
                            )
                            .build();

            participants.add(participant);
        }

        List<ActivityParticipant> savedParticipants =
                participantRepository.saveAll(
                        participants
                );

        participantRepository.flush();

        return savedParticipants
                .stream()
                .map(participantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityParticipantResponse> getParticipants(
            Long activityId
    ) {
        findActivity(activityId);

        return participantRepository
                .findAllByActivity_IdOrderByRegisteredAtDesc(
                        activityId
                )
                .stream()
                .map(participantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void removeParticipant(
            Long activityId,
            Long memberId,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        validateActivityCanBeModified(activity);
        findUser(currentUserId);

        ActivityParticipant participant =
                participantRepository
                        .findByActivity_IdAndMember_Id(
                                activityId,
                                memberId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Activity participant not found"
                                )
                        );

        if (participant.getCheckedInAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    A participant who has already checked in
                    cannot be removed
                    """
            );
        }

        participantRepository.delete(participant);
    }

    private ParticipantAccess resolveParticipantAccess(
            Activity activity,
            Member member
    ) {
        Long memberBranchId = member.getBranchId();

        if (memberBranchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member "
                            + member.getId()
                            + " does not belong to a branch"
            );
        }

        Long hostBranchId = activity.getBranchId();

        boolean sameBranch =
                hostBranchId != null
                        && hostBranchId.equals(memberBranchId);

        String activityTypeCode =
                activity.getType() != null
                        ? activity.getType().getCode()
                        : null;

        boolean internalActivity =
                "INTERNAL".equalsIgnoreCase(activityTypeCode);

        boolean externalActivity =
                "EXTERNAL".equalsIgnoreCase(activityTypeCode);

        if (!internalActivity && !externalActivity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported activity type"
            );
        }

        /*
         * INTERNAL activity:
         * only members from the host branch are allowed.
         */
        if (internalActivity) {
            if (!sameBranch) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "An INTERNAL activity may only include "
                                + "members from the host branch"
                );
            }

            return new ParticipantAccess(
                    ParticipantRegistrationSource.HOST_BRANCH,
                    null
            );
        }

        /*
         * EXTERNAL activity:
         * host-branch members are allowed directly.
         */
        if (sameBranch) {
            return new ParticipantAccess(
                    ParticipantRegistrationSource.HOST_BRANCH,
                    null
            );
        }

        /*
         * A member from another branch is allowed only when
         * that branch has accepted the activity invitation.
         */
        ActivityInvitedBranch acceptedInvitation =
                invitedBranchRepository
                        .findByActivity_IdAndBranch_IdAndInvitationStatus(
                                activity.getId(),
                                memberBranchId,
                                ActivityInvitationStatus.ACCEPTED
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "The member's branch has not "
                                                + "accepted an invitation for "
                                                + "this external activity"
                                )
                        );

        return new ParticipantAccess(
                ParticipantRegistrationSource.INVITED_BRANCH,
                acceptedInvitation
        );
    }

    private void validateCapacity(
            Activity activity,
            int numberOfNewParticipants
    ) {
        if (activity.getCapacity() == null) {
            return;
        }

        long existingParticipants =
                participantRepository
                        .countByActivity_Id(
                                activity.getId()
                        );

        long total =
                existingParticipants
                        + numberOfNewParticipants;

        if (total > activity.getCapacity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Activity capacity would be exceeded"
            );
        }
    }

    private Activity findActivity(Long activityId) {
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

    private User findUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user ID is required"
            );
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );
    }

    private void validateActivityCanBeModified(
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
                        .toUpperCase();

        if ("COMPLETED".equals(statusCode)
                || "CANCELLED".equals(statusCode)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    A completed or cancelled activity
                    cannot be modified
                    """
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

    private record ParticipantAccess(
            ParticipantRegistrationSource registrationSource,
            ActivityInvitedBranch invitedBranch
    ) {
    }
}