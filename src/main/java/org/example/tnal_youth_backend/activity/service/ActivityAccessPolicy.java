package org.example.tnal_youth_backend.activity.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;
import org.example.tnal_youth_backend.activity.repository.ActivityInvitedBranchRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityParticipantRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ActivityAccessPolicy {

    private final UserRepository userRepository;
    private final ActivityInvitedBranchRepository invitedBranchRepository;
    private final ActivityParticipantRepository participantRepository;

    public User requireUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    public void requireCanCreateForBranch(User user, Long branchId) {
        requireBranchStaff(user);
        if (user.getBranchId() == null || !user.getBranchId().equals(branchId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Branch staff can only create activities for their own branch");
        }
    }

    public void requireCanManageHostActivity(User user, Activity activity) {
        requireBranchStaff(user);
        if (user.getBranchId() == null || !user.getBranchId().equals(activity.getBranchId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only staff from the host branch can modify this activity");
        }
    }

    public void requireCanInviteMembers(User user, Activity activity) {
        requireBranchStaff(user);
        if (user.getBranchId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff account has no branch assignment");
        }
        if (user.getBranchId().equals(activity.getBranchId())) return;
        boolean invited = invitedBranchRepository
                .findByActivity_IdAndBranch_IdAndInvitationStatus(
                        activity.getId(), user.getBranchId(), ActivityInvitationStatus.ACCEPTED)
                .isPresent();
        if (!invited) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only accepted invited-branch staff can invite their members");
        }
    }

    public void requireMemberFromActorBranch(User user, Long memberBranchId) {
        if (user.getBranchId() == null || !user.getBranchId().equals(memberBranchId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Branch staff can only invite members from their own branch");
        }
    }

    public void requireCanView(User user, Activity activity) {
        if (user.getRole() == UserRole.ADMIN) return;
        if ((user.getRole() == UserRole.SECRETARY || user.getRole() == UserRole.BRANCH_LEADER)
                && user.getBranchId() != null
                && (user.getBranchId().equals(activity.getBranchId())
                || invitedBranchRepository.existsByActivity_IdAndBranch_IdAndInvitationStatusNot(
                activity.getId(), user.getBranchId(), ActivityInvitationStatus.CANCELLED))) return;
        if (user.getMemberId() != null
                && participantRepository.existsByActivity_IdAndMember_Id(activity.getId(), user.getMemberId())) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not invited to this activity");
    }

    private void requireBranchStaff(User user) {
        if (user.getRole() != UserRole.SECRETARY && user.getRole() != UserRole.BRANCH_LEADER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only a branch leader or secretary can perform this action");
        }
    }
}
