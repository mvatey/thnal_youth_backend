package org.example.tnal_youth_backend.activity.mapper;

import org.example.tnal_youth_backend.activity.model.entity.ActivityInvitedBranch;
import org.example.tnal_youth_backend.activity.model.response.ActivityInvitedBranchResponse;
import org.springframework.stereotype.Component;

@Component
public class ActivityInvitedBranchMapper {

    public ActivityInvitedBranchResponse toResponse(
            ActivityInvitedBranch invitation
    ) {
        if (invitation == null) {
            return null;
        }

        return ActivityInvitedBranchResponse.builder()
                .id(invitation.getId())
                .activityId(
                        invitation.getActivity() != null
                                ? invitation.getActivity().getId()
                                : null
                )
                .branchId(
                        invitation.getBranch() != null
                                ? invitation.getBranch().getId()
                                : null
                )
                .branchCode(
                        invitation.getBranch() != null
                                ? invitation.getBranch().getBranchCode()
                                : null
                )
                .branchNameKm(
                        invitation.getBranch() != null
                                ? invitation.getBranch().getNameKm()
                                : null
                )
                .branchNameEn(
                        invitation.getBranch() != null
                                ? invitation.getBranch().getNameEn()
                                : null
                )
                .invitationStatus(
                        invitation.getInvitationStatus()
                )
                .canManageAttendance(
                        invitation.getCanManageAttendance()
                )
                .canRecordDonation(
                        invitation.getCanRecordDonation()
                )
                .invitedBy(
                        invitation.getInvitedBy() != null
                                ? invitation.getInvitedBy().getId()
                                : null
                )
                .invitedAt(invitation.getInvitedAt())
                .respondedBy(
                        invitation.getRespondedBy() != null
                                ? invitation.getRespondedBy().getId()
                                : null
                )
                .respondedAt(invitation.getRespondedAt())
                .note(invitation.getNote())
                .createdAt(invitation.getCreatedAt())
                .updatedAt(invitation.getUpdatedAt())
                .build();
    }
}