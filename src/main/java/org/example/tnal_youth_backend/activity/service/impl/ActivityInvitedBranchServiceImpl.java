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
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityInvitedBranchServiceImpl
        implements ActivityInvitedBranchService {

    private final ActivityRepository activityRepository;
    private final ActivityInvitedBranchRepository
            invitedBranchRepository;

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    private final ActivityInvitedBranchMapper
            invitedBranchMapper;

    @Override
    @Transactional
    public ActivityInvitedBranchResponse inviteBranch(
            Long activityId,
            InviteBranchRequest request,
            Long currentUserId
    ) {
        Activity activity = findActivity(activityId);

        validateExternalActivity(activity);
        validateActivityCanBeModified(activity);

        Branch branch = findBranch(request.getBranchId());
        User invitedBy = findUser(currentUserId);

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
                        .invitedAt(OffsetDateTime.now())
                        .note(trimToNull(request.getNote()))
                        .build();

        try {
            ActivityInvitedBranch savedInvitation =
                    invitedBranchRepository.saveAndFlush(
                            invitation
                    );

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

        invitation.setInvitationStatus(responseStatus);
        invitation.setRespondedBy(respondedBy);
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

        findUser(currentUserId);

        invitation.setInvitationStatus(
                ActivityInvitationStatus.CANCELLED
        );

        invitation.setRespondedAt(
                OffsetDateTime.now()
        );

        /*
         * respondedBy is intentionally left unchanged here.
         *
         * Later, if you add cancelled_by and cancelled_at
         * columns, use those fields instead.
         */

        invitedBranchRepository.saveAndFlush(
                invitation
        );
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

    private Branch findBranch(Long branchId) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        return branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Branch not found with ID: "
                                        + branchId
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

    private ActivityInvitedBranch findInvitation(
            Long activityId,
            Long invitationId
    ) {
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

    private void validateExternalActivity(
            Activity activity
    ) {
        if (activity.getType() == null
                || activity.getType().getCode() == null
                || !"EXTERNAL".equalsIgnoreCase(
                activity.getType().getCode()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Other branches can only be invited
                    to an EXTERNAL activity
                    """
            );
        }
    }

    private void validateBranchIsNotHostBranch(
            Activity activity,
            Branch invitedBranch
    ) {
        if (activity.getBranchId() != null
                && activity.getBranchId()
                .equals(invitedBranch.getId())) {

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
                    "A completed or cancelled activity cannot be modified"
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

        invitation.setInvitedBy(invitedBy);
        invitation.setInvitedAt(
                OffsetDateTime.now()
        );

        invitation.setRespondedBy(null);
        invitation.setRespondedAt(null);
        invitation.setNote(
                trimToNull(request.getNote())
        );
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
}