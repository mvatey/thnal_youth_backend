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

    private final ActivityParticipantMapper
            participantMapper;

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

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Participant invitation request is required"
            );
        }

        if (request.getMemberIds() == null
                || request.getMemberIds().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one member ID is required"
            );
        }

        /*
         * LinkedHashSet removes duplicate IDs from the request
         * while preserving the original request order.
         */
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

        /*
         * Confirm that every requested member exists.
         */
        if (members.size()
                != requestedMemberIds.size()) {

            Set<Long> foundIds =
                    new LinkedHashSet<>();

            for (Member member : members) {
                foundIds.add(
                        member.getId()
                );
            }

            Set<Long> missingIds =
                    new LinkedHashSet<>(
                            requestedMemberIds
                    );

            missingIds.removeAll(
                    foundIds
            );

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Members not found: " + missingIds
            );
        }

        /*
         * Check duplicate participants before capacity
         * validation so only truly new members are considered.
         */
        for (Member member : members) {
            boolean alreadyParticipant =
                    participantRepository
                            .existsByActivity_IdAndMember_Id(
                                    activityId,
                                    member.getId()
                            );

            if (alreadyParticipant) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Member "
                                + member.getId()
                                + " is already a participant"
                );
            }
        }

        validateCapacity(
                activity,
                members.size()
        );

        List<ActivityParticipant> participants =
                new ArrayList<>();

        for (Member member : members) {
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

            participants.add(
                    participant
            );
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
        Activity activity = findActivity(
                activityId
        );

        validateActivityCanBeModified(
                activity
        );

        findUser(currentUserId);

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

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

        /*
         * A participant who has already checked in must remain
         * in the activity participation history.
         */
        if (participant.getCheckedInAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    A participant who has already checked in
                    cannot be removed
                    """
            );
        }

        participantRepository.delete(
                participant
        );
    }

    private ParticipantAccess resolveParticipantAccess(
            Activity activity,
            Member member
    ) {
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

        Long hostBranchId =
                activity.getBranchId();

        if (hostBranchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The activity does not have a host branch"
            );
        }

        boolean isHostBranchMember =
                hostBranchId.equals(
                        memberBranchId
                );

        /*
         * Members from the activity's host branch can be
         * invited directly.
         *
         * This rule applies to both:
         *
         * INTERNAL
         * EXTERNAL
         */
        if (isHostBranchMember) {
            return new ParticipantAccess(
                    ParticipantRegistrationSource.HOST_BRANCH,
                    null
            );
        }

        /*
         * Members from another branch can be invited only
         * after that branch accepts its activity invitation.
         *
         * This rule also applies to both:
         *
         * INTERNAL
         * EXTERNAL
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
                                                + "accepted an invitation "
                                                + "for this activity"
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

        if (activity.getCapacity() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Activity capacity cannot be negative"
            );
        }

        long existingParticipants =
                participantRepository
                        .countByActivity_Id(
                                activity.getId()
                        );

        long totalParticipants =
                existingParticipants
                        + numberOfNewParticipants;

        if (totalParticipants
                > activity.getCapacity()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Activity capacity would be exceeded"
            );
        }
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

    private User findUser(
            Long userId
    ) {
        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user ID is required"
            );
        }

        return userRepository
                .findById(userId)
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

    private record ParticipantAccess(
            ParticipantRegistrationSource registrationSource,
            ActivityInvitedBranch invitedBranch
    ) {
    }
}