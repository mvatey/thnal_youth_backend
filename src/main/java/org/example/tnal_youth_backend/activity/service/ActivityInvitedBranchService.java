package org.example.tnal_youth_backend.activity.service;

import org.example.tnal_youth_backend.activity.model.request.InviteBranchRequest;
import org.example.tnal_youth_backend.activity.model.request.NotifyCertificateBranchesRequest;
import org.example.tnal_youth_backend.activity.model.request.RespondBranchInvitationRequest;
import org.example.tnal_youth_backend.activity.model.response.ActivityBranchResponse;
import org.example.tnal_youth_backend.activity.model.response.ActivityInvitedBranchResponse;
import org.example.tnal_youth_backend.activity.model.response.CertificateBranchNotifyResponse;

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

    /**
     * Tells the leadership (SECRETARY/BRANCH_LEADER) of each given branch —
     * every one of which must be an ACCEPTED co-host of this activity, and
     * none of which may be the activity's own host branch — that
     * certificates from this activity are ready for their branch's own
     * members. Only the host branch's own SECRETARY/BRANCH_LEADER may call
     * this (same authorization as inviting/cancelling a co-hosting branch).
     * Individual branch members are never notified directly — only their
     * branch's leadership, who is expected to distribute the certificates
     * within their own branch.
     */
    CertificateBranchNotifyResponse notifyBranchesCertificatesReady(
            Long activityId,
            NotifyCertificateBranchesRequest request,
            Long currentUserId
    );
}