package org.example.tnal_youth_backend.member.member.security;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.example.tnal_youth_backend.security.ViewerAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MemberAccessValidator {

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;

    private final StaffBranchScopeService staffBranchScopeService;

    private final ViewerAccessService viewerAccessService;

    public Member validateAccessibleMember(
            Long memberId
    ) {
        Member targetMember =
                findMember(memberId);

        User currentUser =
                getCurrentUser();

        UserRole role =
                viewerAccessService.effectiveReadRole(currentUser);

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        /*
         * Admin can access all members.
         */
        if (role == UserRole.ADMIN) {
            return targetMember;
        }

        /*
         * Member can access only their own profile.
         */
        if (role == UserRole.MEMBER) {
            validateOwnMemberAccess(
                    currentUser,
                    memberId
            );

            return targetMember;
        }

        /*
         * Secretary and Branch Leader use branch scope.
         */
        validateManagementBranchAccess(
                currentUser,
                targetMember.getBranchId()
        );

        return targetMember;
    }

    public void validateBranchAccess(
            Long branchId
    ) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        User currentUser =
                getCurrentUser();

        UserRole role =
                viewerAccessService.effectiveReadRole(currentUser);

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        if (role == UserRole.ADMIN) {
            return;
        }

        validateManagementBranchAccess(
                currentUser,
                branchId
        );
    }

    private void validateOwnMemberAccess(
            User currentUser,
            Long targetMemberId
    ) {
        Long currentMemberId =
                currentUser.getMemberId();

        if (currentMemberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is not linked to a member record"
            );
        }

        if (!currentMemberId.equals(
                targetMemberId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You may access only your own member profile"
            );
        }
    }

    private void validateManagementBranchAccess(
            User currentUser,
            Long branchId
    ) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        UserRole role = viewerAccessService.effectiveReadRole(currentUser);

        if (role != UserRole.SECRETARY
                && role != UserRole.BRANCH_LEADER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to manage members"
            );
        }

        /*
         * Central Phase-3 rule:
         * SECRETARY -> home + all active assigned branches.
         * BRANCH_LEADER -> exactly one branch only.
         */
        staffBranchScopeService.requireStaffBranchAccess(
                currentUser,
                branchId
        );
    }

    private User getCurrentUser() {
        User principalUser =
                SecurityUtil.getCurrentUser();

        if (principalUser == null
                || principalUser.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return userRepository
                .findById(
                        principalUser.getId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );
    }

    private Member findMember(
            Long memberId
    ) {
        if (memberId == null
                || memberId <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID must be greater than zero"
            );
        }

        return memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    public void validateCanManageSensitiveFields(
            Long targetMemberId
    ) {
        Member targetMember =
                validateAccessibleMember(
                        targetMemberId
                );

        User currentUser =
                getCurrentUser();

        UserRole actorRole =
                viewerAccessService.effectiveReadRole(currentUser);

        if (actorRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        /*
         * Admin may change branch, role, and account status
         * for any accessible member.
         */
        if (actorRole == UserRole.ADMIN) {
            return;
        }

        Long actorMemberId =
                currentUser.getMemberId();

        /*
         * Secretary and Branch Leader cannot change
         * their own sensitive fields.
         */
        if (
                actorMemberId != null
                        && actorMemberId.equals(
                        targetMemberId
                )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot change your own branch, role, or account status"
            );
        }

        User targetUser =
                userRepository
                        .findByMemberId(
                                targetMember.getId()
                        )
                        .orElse(null);

        /*
         * A member without an account is treated as MEMBER.
         */
        UserRole targetRole =
                targetUser == null
                        || targetUser.getRole() == null
                        ? UserRole.MEMBER
                        : targetUser.getRole();

        /*
         * Branch Leader may manage sensitive fields
         * for Secretary and Member only.
         */
        if (actorRole == UserRole.BRANCH_LEADER) {
            if (
                    targetRole == UserRole.SECRETARY
                            || targetRole == UserRole.MEMBER
            ) {
                return;
            }
        }

        /*
         * Secretary may manage sensitive fields
         * for Member only.
         */
        if (actorRole == UserRole.SECRETARY) {
            if (targetRole == UserRole.MEMBER) {
                return;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to change this member's branch, role, or account status"
        );
    }

    /**
     * Validates an explicit branch assignment/removal operation.
     *
     * This is intentionally different from validateCanManageSensitiveFields():
     * assigning the first branch to a secretary is valid even when the
     * secretary currently has no primary branch, so validation cannot depend
     * on targetMember.branchId being non-null.
     */
    public void validateCanAssignBranch(
            Long memberId,
            Long branchId
    ) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        Member targetMember = findMember(memberId);
        User currentUser = getCurrentUser();
        UserRole actorRole = currentUser.getRole();

        if (actorRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        User targetUser = userRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "This member does not have a user account"
                ));

        if (targetUser.getRole() != UserRole.SECRETARY) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only secretary accounts can be assigned to additional branches"
            );
        }

        if (actorRole == UserRole.ADMIN) {
            return;
        }

        if (actorRole != UserRole.BRANCH_LEADER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only an admin or branch leader can assign branches"
            );
        }

        // A branch leader can assign/remove only within their own branch scope.
        staffBranchScopeService.requireStaffBranchAccess(
                currentUser,
                branchId
        );

        // If the secretary already has a primary branch, the branch leader
        // must also be able to manage that member. A secretary with no primary
        // branch is intentionally allowed so the first assignment can create it.
        if (targetMember.getBranchId() != null) {
            validateManagementBranchAccess(
                    currentUser,
                    targetMember.getBranchId()
            );
        }
    }

    public boolean isCurrentUserAdmin() {
        return getCurrentUser().getRole() == UserRole.ADMIN;
    }
}
