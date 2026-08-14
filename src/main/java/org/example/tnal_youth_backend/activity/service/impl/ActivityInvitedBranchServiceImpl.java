package org.example.tnal_youth_backend.activity.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.mapper.ActivityInvitedBranchMapper;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.entity.ActivityInvitedBranch;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.activity.model.request.InviteBranchRequest;
import org.example.tnal_youth_backend.activity.model.request.RespondBranchInvitationRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityInvitedBranchResponse;
import org.example.tnal_youth_backend.activity.repository.ActivityInvitedBranchRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.activity.service.ActivityInvitedBranchService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateDTO;
import org.example.tnal_youth_backend.notification.repo.NotificationRepo;
import org.example.tnal_youth_backend.notification.service.NotificationService;
import org.springframework.dao.DataIntegrityViolationException;
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
public class ActivityInvitedBranchServiceImpl
        implements ActivityInvitedBranchService {

    private final ActivityRepository activityRepository;

    private final ActivityInvitedBranchRepository
            invitedBranchRepository;

    private final BranchRepository branchRepository;

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;

    private final BranchStaffRepository branchStaffRepository;

    private final NotificationRepo notificationRepo;

    private final NotificationService notificationService;

    private final ActivityInvitedBranchMapper
            invitedBranchMapper;

    private static final String BRANCH_INVITATION_TYPE_CODE =
            "ACTIVITY_BRANCH_INVITATION";

    @Override
    @Transactional
    public ActivityInvitedBranchResponse inviteBranch(
            Long activityId,
            InviteBranchRequest request,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        /*
         * Both INTERNAL and EXTERNAL activities can invite
         * another branch.
         *
         * Activity type must not restrict branch invitation.
         */
        validateActivityCanBeModified(activity);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invitation request is required"
            );
        }

        Branch branch = findBranch(
                request.getBranchId()
        );

        User invitedBy = findUser(currentUserId);

        /*
         * Only the activity's own host branch leadership may invite
         * another branch to co-host it.
         */
        validateHostManagePermission(activity, invitedBy);

        /*
         * The activity host branch must not invite itself.
         *
         * Members from the host branch are already directly
         * available for participant invitation.
         */
        validateBranchIsNotHostBranch(
                activity,
                branch
        );

        ActivityInvitedBranch existingInvitation =
                invitedBranchRepository
                        .findByActivity_IdAndBranch_Id(
                                activityId,
                                branch.getId()
                        )
                        .orElse(null);

        /*
         * A branch can only have one invitation record
         * for an activity.
         *
         * A cancelled invitation can be reactivated.
         */
        if (existingInvitation != null) {
            if (existingInvitation.getInvitationStatus()
                    != ActivityInvitationStatus.CANCELLED) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This branch has already been invited"
                );
            }

            reactivateInvitation(
                    existingInvitation,
                    request,
                    invitedBy
            );

            ActivityInvitedBranch savedInvitation =
                    invitedBranchRepository.saveAndFlush(
                            existingInvitation
                    );

            notifyBranchInvited(activity, branch);

            return invitedBranchMapper.toResponse(
                    savedInvitation
            );
        }

        ActivityInvitedBranch invitation =
                ActivityInvitedBranch.builder()
                        .activity(activity)
                        .branch(branch)
                        .invitationStatus(
                                ActivityInvitationStatus.PENDING
                        )
                        .canManageAttendance(
                                Boolean.TRUE.equals(
                                        request.getCanManageAttendance()
                                )
                        )
                        .canRecordDonation(
                                Boolean.TRUE.equals(
                                        request.getCanRecordDonation()
                                )
                        )
                        .invitedBy(invitedBy)
                        .invitedAt(
                                OffsetDateTime.now()
                        )
                        .note(
                                trimToNull(
                                        request.getNote()
                                )
                        )
                        .build();

        try {
            ActivityInvitedBranch savedInvitation =
                    invitedBranchRepository.saveAndFlush(
                            invitation
                    );

            notifyBranchInvited(activity, branch);

            return invitedBranchMapper.toResponse(
                    savedInvitation
            );

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This branch has already been invited"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityInvitedBranchResponse>
    getInvitedBranches(
            Long activityId
    ) {
        findActivity(activityId);

        return invitedBranchRepository
                .findAllByActivity_IdOrderByInvitedAtDesc(
                        activityId
                )
                .stream()
                .map(invitedBranchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ActivityInvitedBranchResponse respondToInvitation(
            Long activityId,
            Long invitationId,
            RespondBranchInvitationRequest request,
            Long currentUserId
    ) {
        ActivityInvitedBranch invitation =
                findInvitation(
                        activityId,
                        invitationId
                );

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invitation response request is required"
            );
        }

        if (invitation.getInvitationStatus()
                != ActivityInvitationStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only a pending invitation can be answered"
            );
        }

        ActivityInvitationStatus responseStatus =
                request.getInvitationStatus();

        if (responseStatus
                != ActivityInvitationStatus.ACCEPTED
                && responseStatus
                != ActivityInvitationStatus.DECLINED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Invitation response must be ACCEPTED
                    or DECLINED
                    """
            );
        }

        User respondedBy = findUser(currentUserId);

        /*
         * Only a branch leader/secretary of the INVITED branch itself may
         * accept or decline on its behalf — not the host, and not some
         * other branch's staff.
         */
        validateInvitedBranchPermission(invitation, respondedBy);

        invitation.setInvitationStatus(
                responseStatus
        );

        invitation.setRespondedBy(
                respondedBy
        );

        invitation.setRespondedAt(
                OffsetDateTime.now()
        );

        ActivityInvitedBranch savedInvitation =
                invitedBranchRepository.saveAndFlush(
                        invitation
                );

        return invitedBranchMapper.toResponse(
                savedInvitation
        );
    }

    @Override
    @Transactional
    public void cancelInvitation(
            Long activityId,
            Long invitationId,
            Long currentUserId
    ) {
        ActivityInvitedBranch invitation =
                findInvitation(
                        activityId,
                        invitationId
                );

        validateActivityCanBeModified(
                invitation.getActivity()
        );

        if (invitation.getInvitationStatus()
                == ActivityInvitationStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invitation is already cancelled"
            );
        }

        /*
         * Only the host branch's own leadership may withdraw an invitation
         * they sent.
         */
        validateHostManagePermission(
                invitation.getActivity(),
                findUser(currentUserId)
        );

        invitation.setInvitationStatus(
                ActivityInvitationStatus.CANCELLED
        );

        invitation.setRespondedAt(
                OffsetDateTime.now()
        );

        /*
         * respondedBy is intentionally not changed here.
         *
         * A future migration may add:
         *
         * cancelled_by
         * cancelled_at
         */
        invitedBranchRepository.saveAndFlush(
                invitation
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

    private Branch findBranch(
            Long branchId
    ) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        return branchRepository
                .findById(branchId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Branch not found with ID: "
                                        + branchId
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

    private ActivityInvitedBranch findInvitation(
            Long activityId,
            Long invitationId
    ) {
        if (activityId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity ID is required"
            );
        }

        if (invitationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invitation ID is required"
            );
        }

        return invitedBranchRepository
                .findByIdAndActivity_Id(
                        invitationId,
                        activityId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Activity branch invitation not found"
                        )
                );
    }

    /**
     * Only a branch leader or secretary who is staff of the activity's own
     * host branch may invite/cancel a co-hosting branch invitation.
     */
    private void validateHostManagePermission(
            Activity activity,
            User currentUser
    ) {
        if (currentUser.getRole() != UserRole.BRANCH_LEADER
                && currentUser.getRole() != UserRole.SECRETARY) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only a branch leader or secretary can manage "
                            + "branch invitations for this activity"
            );
        }

        Set<Long> staffBranchIds = resolveStaffBranchIds(currentUser);

        if (activity.getBranchId() == null
                || !staffBranchIds.contains(activity.getBranchId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the activity's own host branch can manage its "
                            + "branch invitations"
            );
        }
    }

    /**
     * Only a branch leader or secretary who is staff of the INVITED branch
     * itself may accept/decline on its behalf.
     */
    private void validateInvitedBranchPermission(
            ActivityInvitedBranch invitation,
            User currentUser
    ) {
        if (currentUser.getRole() != UserRole.BRANCH_LEADER
                && currentUser.getRole() != UserRole.SECRETARY) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only a branch leader or secretary can respond to a "
                            + "branch invitation"
            );
        }

        Long invitedBranchId =
                invitation.getBranch() != null
                        ? invitation.getBranch().getId()
                        : null;

        Set<Long> staffBranchIds = resolveStaffBranchIds(currentUser);

        if (invitedBranchId == null
                || !staffBranchIds.contains(invitedBranchId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the invited branch's own staff can respond to "
                            + "this invitation"
            );
        }
    }

    private Set<Long> resolveStaffBranchIds(User user) {
        if (user.getMemberId() == null) {
            return Set.of();
        }

        Set<Long> branchIds = new LinkedHashSet<>(
                branchStaffRepository.findActiveBranchIdsByMemberId(
                        user.getMemberId()
                )
        );

        memberRepository.findById(user.getMemberId())
                .map(Member::getBranchId)
                .ifPresent(branchIds::add);

        return branchIds;
    }

    /**
     * Sends an in-app "your branch was invited" notification to the invited
     * branch's leader/secretary user accounts. Mirrors
     * {@code ActivityParticipantServiceImpl.notifyInvitedMembers} — a
     * notification failure must never roll back the invitation itself, so
     * any error here is swallowed rather than propagated.
     */
    private void notifyBranchInvited(
            Activity activity,
            Branch branch
    ) {
        Set<Long> userIds =
                branchStaffRepository.findActiveStaffUserIds(
                        branch.getId()
                );

        if (userIds.isEmpty()) {
            return;
        }

        Short typeId = notificationRepo.findActiveTypeIdByCode(
                BRANCH_INVITATION_TYPE_CODE
        );

        if (typeId == null) {
            return;
        }

        try {
            NotificationCreateDTO notification =
                    new NotificationCreateDTO();
            notification.setTypeId(typeId);
            notification.setTitle(
                    "សាខារបស់អ្នកត្រូវបានអញ្ជើញចូលរួមកម្មវិធី"
            );
            notification.setBody(
                    "សាខារបស់អ្នកត្រូវបានអញ្ជើញឱ្យចូលរួមរៀបចំកម្មវិធី \""
                            + activity.getTitleKm()
                            + "\""
            );
            notification.setActionUrl(
                    "/activity/" + activity.getId()
            );
            notification.setActivityId(activity.getId());
            notification.setTarget(
                    NotificationCreateDTO.TargetMode.USERS
            );
            notification.setTargetUserIds(
                    new ArrayList<>(userIds)
            );

            notificationService.create(notification);
        } catch (RuntimeException ignored) {
            /*
             * A notification failure must not fail the branch invitation
             * that already succeeded and was flushed to the database.
             */
        }
    }

    private void validateBranchIsNotHostBranch(
            Activity activity,
            Branch invitedBranch
    ) {
        Long hostBranchId = activity.getBranchId();

        if (hostBranchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The activity does not have a host branch"
            );
        }

        if (hostBranchId.equals(
                invitedBranch.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    The activity host branch cannot be
                    invited to its own activity
                    """
            );
        }
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

    private void reactivateInvitation(
            ActivityInvitedBranch invitation,
            InviteBranchRequest request,
            User invitedBy
    ) {
        invitation.setInvitationStatus(
                ActivityInvitationStatus.PENDING
        );

        invitation.setCanManageAttendance(
                Boolean.TRUE.equals(
                        request.getCanManageAttendance()
                )
        );

        invitation.setCanRecordDonation(
                Boolean.TRUE.equals(
                        request.getCanRecordDonation()
                )
        );

        invitation.setInvitedBy(
                invitedBy
        );

        invitation.setInvitedAt(
                OffsetDateTime.now()
        );

        invitation.setRespondedBy(null);
        invitation.setRespondedAt(null);

        invitation.setNote(
                trimToNull(
                        request.getNote()
                )
        );
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}