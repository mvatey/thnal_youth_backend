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
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateDTO;
import org.example.tnal_youth_backend.notification.repo.NotificationRepo;
import org.example.tnal_youth_backend.notification.service.NotificationService;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
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

    private final StaffBranchScopeService staffBranchScopeService;

    private final ActivityRepository activityRepository;

    private final ActivityParticipantRepository
            participantRepository;

    private final ActivityInvitedBranchRepository
            invitedBranchRepository;

    private final MemberRepository memberRepository;

    private final UserRepository userRepository;

    private final BranchStaffRepository branchStaffRepository;

    private final ActivityParticipantMapper
            participantMapper;

    private final NotificationRepo notificationRepo;

    private final NotificationService notificationService;

    private static final String ACTIVITY_INVITATION_TYPE_CODE = "ACTIVITY_INVITATION";

    @Override
    @Transactional
    public List<ActivityParticipantResponse> inviteParticipants(
            Long activityId,
            InviteParticipantsRequest request,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        User invitedBy = findUser(currentUserId);

        ManagePermission managePermission =
                resolveManagePermission(activity, invitedBy);

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
         * A co-hosting (invited) branch's staff may only invite members of
         * their OWN branch — never the host branch's members or another
         * invited branch's members.
         */
        if (!managePermission.fullAccess()) {
            for (Member member : members) {
                if (!managePermission.restrictedToBranchId()
                        .equals(member.getBranchId())) {

                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "You can only invite members of your own branch"
                    );
                }
            }
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

        notifyInvitedMembers(
                activity,
                savedParticipants
        );

        return savedParticipants
                .stream()
                .map(participantMapper::toResponse)
                .toList();
    }

    /**
     * Sends an in-app "you were invited" notification to every newly-invited
     * participant that has a linked user account (users.member_id). Members
     * without a user account (e.g. not yet activated) are silently skipped —
     * there is nowhere to deliver an in-app notification for them.
     *
     * <p>Failure to notify must never roll back the invitation itself, so any
     * notification error is swallowed here rather than propagated.
     */
    private void notifyInvitedMembers(
            Activity activity,
            List<ActivityParticipant> savedParticipants
    ) {
        if (savedParticipants == null
                || savedParticipants.isEmpty()) {
            return;
        }

        List<Long> userIds = new ArrayList<>();

        for (ActivityParticipant participant : savedParticipants) {
            Member member = participant.getMember();

            if (member == null || member.getId() == null) {
                continue;
            }

            userRepository
                    .findByMemberId(member.getId())
                    .ifPresent(user -> userIds.add(user.getId()));
        }

        if (userIds.isEmpty()) {
            return;
        }

        Short typeId =
                notificationRepo.findActiveTypeIdByCode(
                        ACTIVITY_INVITATION_TYPE_CODE
                );

        if (typeId == null) {
            return;
        }

        try {
            NotificationCreateDTO notification = new NotificationCreateDTO();
            String titleEn = activity.getTitleEn() != null && !activity.getTitleEn().isBlank()
                    ? activity.getTitleEn()
                    : activity.getTitleKm();

            notification.setTypeId(typeId);
            notification.setTitle("អ្នកត្រូវបានអញ្ជើញចូលរួមកម្មវិធី");
            notification.setBody(
                    "អ្នកត្រូវបានអញ្ជើញឱ្យចូលរួមក្នុងកម្មវិធី \""
                            + activity.getTitleKm()
                            + "\""
            );
            notification.setTitleEn("You've Been Invited to an Activity");
            notification.setBodyEn(
                    "You have been invited to join the activity \""
                            + titleEn
                            + "\""
            );
            notification.setActionUrl("/activity/" + activity.getId());
            notification.setActivityId(activity.getId());
            notification.setTarget(NotificationCreateDTO.TargetMode.USERS);
            notification.setTargetUserIds(userIds);

            notificationService.create(notification);
        } catch (RuntimeException ignored) {
            /*
             * A notification failure must not fail the invitation that
             * already succeeded and was flushed to the database.
             */
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityParticipantResponse> getParticipants(
            Long activityId,
            Long currentUserId
    ) {
        findActivity(activityId);

        User currentUser = findUser(currentUserId);

        /*
         * Members only get the activity's basic detail and its documents —
         * the participant/attendance roster is management information they
         * do not need, regardless of whether they were invited themselves.
         */
        if (currentUser.getRole() == UserRole.MEMBER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Members cannot view the activity participant list"
            );
        }

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

        User currentUser = findUser(currentUserId);

        ManagePermission managePermission =
                resolveManagePermission(activity, currentUser);

        validateActivityCanBeModified(
                activity
        );

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
         * A co-hosting (invited) branch's staff may only remove members of
         * their OWN branch from the activity.
         */
        if (!managePermission.fullAccess()) {
            Member participantMember = participant.getMember();

            if (participantMember == null
                    || !managePermission.restrictedToBranchId()
                    .equals(participantMember.getBranchId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only remove members of your own branch"
                );
            }
        }

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

    /**
     * Who may invite/remove participants on this activity, and how far that
     * reaches:
     *
     * <ul>
     *   <li>the host branch's own branch leader/secretary — full access,
     *       {@code restrictedToBranchId} is {@code null};</li>
     *   <li>a branch leader/secretary of a branch with an ACCEPTED
     *       invitation to this activity — restricted access, may only
     *       touch participants from {@code restrictedToBranchId} (their
     *       own branch);</li>
     *   <li>anyone else — denied (throws).</li>
     * </ul>
     */
    private record ManagePermission(
            boolean fullAccess,
            Long restrictedToBranchId
    ) {
    }

    private ManagePermission resolveManagePermission(
            Activity activity,
            User currentUser
    ) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return new ManagePermission(true, null);
        }

        if (currentUser.getRole() != UserRole.BRANCH_LEADER
                && currentUser.getRole() != UserRole.SECRETARY) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only an administrator, branch leader, or secretary can manage "
                            + "activity participants"
            );
        }

        Set<Long> staffBranchIds =
                resolveStaffBranchIds(currentUser);

        if (activity.getBranchId() != null
                && staffBranchIds.contains(activity.getBranchId())) {

            return new ManagePermission(true, null);
        }

        for (Long staffBranchId : staffBranchIds) {
            boolean accepted = invitedBranchRepository
                    .findByActivity_IdAndBranch_IdAndInvitationStatus(
                            activity.getId(),
                            staffBranchId,
                            ActivityInvitationStatus.ACCEPTED
                    )
                    .isPresent();

            if (accepted) {
                return new ManagePermission(false, staffBranchId);
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only manage participants for activities hosted "
                        + "by your own branch, or that your branch has "
                        + "accepted an invitation to"
        );
    }

    private Set<Long> resolveStaffBranchIds(User user) {
        return staffBranchScopeService.staffBranchIds(user);
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
