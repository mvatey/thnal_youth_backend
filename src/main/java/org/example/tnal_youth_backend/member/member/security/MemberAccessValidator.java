package org.example.tnal_youth_backend.member.member.security;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
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

    private final BranchStaffRepository
            branchStaffRepository;

    public Member validateAccessibleMember(
            Long memberId
    ) {
        Member targetMember =
                findMember(memberId);

        User currentUser =
                getCurrentUser();

        UserRole role =
                currentUser.getRole();

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
                currentUser.getRole();

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

        UserRole role =
                currentUser.getRole();

        if (
                role != UserRole.SECRETARY
                        && role != UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to manage members"
            );
        }

        Long currentMemberId =
                currentUser.getMemberId();

        if (currentMemberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is not linked to a member record"
            );
        }

        Set<Long> accessibleBranchIds =
                new LinkedHashSet<>(
                        branchStaffRepository
                                .findActiveBranchIdsByMemberId(
                                        currentMemberId
                                )
                );

        /*
         * Temporary fallback when branch_staff
         * has not been configured yet.
         */
        if (accessibleBranchIds.isEmpty()) {
            Member currentMember =
                    findMember(
                            currentMemberId
                    );

            if (currentMember.getBranchId() != null) {
                accessibleBranchIds.add(
                        currentMember.getBranchId()
                );
            }
        }

        if (!accessibleBranchIds.contains(
                branchId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this member's branch"
            );
        }
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
                currentUser.getRole();

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
}