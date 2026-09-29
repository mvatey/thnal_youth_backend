package org.example.tnal_youth_backend.security;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserBranchAssignmentRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Single source of truth for branch-scoped staff authorization.
 *
 * SECRETARY: home branch + every active branch_staff assignment.
 * BRANCH_LEADER: exactly one branch only (users.branch_id, falling back to members.branch_id).
 *
 * ADMIN/VIEWER/MEMBER are intentionally not resolved here; callers keep their
 * existing role-specific behavior.
 */
@Service
@RequiredArgsConstructor
public class StaffBranchScopeService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final BranchStaffRepository branchStaffRepository;
    private final UserBranchAssignmentRepository userBranchAssignmentRepository;
    private final ViewerAccessService viewerAccessService;

    public Set<Long> currentStaffBranchIds() {
        User principal = SecurityUtil.getCurrentUser();
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved");
        }

        User currentUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"));

        return staffBranchIds(currentUser);
    }

    public Set<Long> staffBranchIds(User user) {
        if (user == null || user.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role");
        }

        UserRole effectiveRole = viewerAccessService.effectiveReadRole(user);

        if (viewerAccessService.isViewer(user) &&
                (effectiveRole == UserRole.SECRETARY || effectiveRole == UserRole.BRANCH_LEADER)) {
            if (user.getBranchId() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Viewer branch scope is missing a branch");
            }
            return Set.of(user.getBranchId());
        }

        if (effectiveRole == UserRole.SECRETARY) {
            return secretaryBranchIds(user);
        }

        if (effectiveRole == UserRole.BRANCH_LEADER) {
            Long branchId = branchLeaderBranchId(user);
            return Set.of(branchId);
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This role does not use staff branch scope");
    }

    public void requireStaffBranchAccess(User user, Long branchId) {
        if (branchId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Branch ID is required");
        }
        if (!staffBranchIds(user).contains(branchId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have access to this branch");
        }
    }

    private Set<Long> secretaryBranchIds(User user) {
        LinkedHashSet<Long> branchIds = new LinkedHashSet<>();

        /*
         * Member-linked secretary:
         * members.branch_id is the home/primary branch and branch_staff
         * contains every active additional assignment. Do NOT also trust
         * users.branch_id here because it can become stale after a member
         * branch change and would accidentally keep access to an old branch.
         *
         * Standalone secretary:
         * user_branch_assignments holds the full covered-branch list (see
         * UserBranchAssignment's own docblock) -- fall back to the single
         * users.branch_id for an account created before that table
         * existed, or one that was never given any extra branches.
         */
        if (user.getMemberId() != null) {
            branchIds.addAll(
                    branchStaffRepository.findActiveBranchIdsByMemberId(user.getMemberId()));

            memberRepository.findById(user.getMemberId())
                    .map(Member::getBranchId)
                    .ifPresent(branchIds::add);
        } else {
            branchIds.addAll(
                    userBranchAssignmentRepository.findBranchIdsByUserId(user.getId()));

            if (branchIds.isEmpty() && user.getBranchId() != null) {
                branchIds.add(user.getBranchId());
            }
        }

        if (branchIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Secretary is not assigned to any branch");
        }

        return Set.copyOf(branchIds);
    }

    private Long branchLeaderBranchId(User user) {
        Long branchId = user.getBranchId();

        if (branchId == null && user.getMemberId() != null) {
            branchId = memberRepository.findById(user.getMemberId())
                    .map(Member::getBranchId)
                    .orElse(null);
        }

        if (branchId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Branch leader account is not assigned to a branch");
        }

        return branchId;
    }
}
