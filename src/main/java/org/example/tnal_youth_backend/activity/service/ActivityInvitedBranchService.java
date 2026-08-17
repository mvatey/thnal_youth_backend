package org.example.tnal_youth_backend.activity.service;

import org.example.tnal_youth_backend.activity.model.request.InviteBranchRequest;
import org.example.tnal_youth_backend.activity.model.request.RespondBranchInvitationRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityBranchResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityInvitedBranchResponse;

import java.util.List;

public interface ActivityInvitedBranchService {

    ActivityInvitedBranchResponse inviteBranch(
            Long activityId,
            InviteBranchRequest request,
            Long currentUserId
    );

    List<ActivityInvitedBranchResponse> getInvitedBranches(
            Long activityId
    );

    /**
     * Every branch connected to this activity — the organizer plus every
     * invited branch — each tagged with its role. See {@link
     * org.example.tnal_youth_backend.activity.model.enums.ActivityBranchRole}.
     */
    List<ActivityBranchResponse> getActivityBranches(
            Long activityId
    );

    ActivityInvitedBranchResponse respondToInvitation(
            Long activityId,
            Long invitationId,
            RespondBranchInvitationRequest request,
            Long currentUserId
    );

    void cancelInvitation(
            Long activityId,
            Long invitationId,
            Long currentUserId
    );
}