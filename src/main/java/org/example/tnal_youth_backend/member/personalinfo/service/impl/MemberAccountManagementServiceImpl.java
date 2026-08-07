package org.example.tnal_youth_backend.member.personalinfo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberAccountManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberAccountManagementServiceImpl
        implements MemberAccountManagementService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MemberAccessValidator memberAccessValidator;
    private final BranchService branchService;

    @Override
    @Transactional
    public void updateRole(
            Long memberId,
            UserRole requestedRole
    ) {
        if (memberId == null || memberId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID must be greater than zero"
            );
        }

        if (requestedRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested role is required"
            );
        }

        /*
         * Validate that the authenticated user
         * may access/manage this member.
         */
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Member not found with ID: "
                                                + memberId
                                )
                        );

        User targetUser =
                userRepository
                        .findByMemberId(memberId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "This member does not have a user account"
                                )
                        );

        User currentUser =
                getCurrentUser();

        UserRole actorRole =
                currentUser.getRole();

        validateAssignableRole(
                actorRole,
                requestedRole
        );

        /*
         * No change required.
         */
        if (
                targetUser.getRole()
                        == requestedRole
        ) {
            return;
        }

        /*
         * =========================================
         * SPECIAL CASE: BRANCH LEADER
         * =========================================
         *
         * Reuse the existing branch feature.
         *
         * assignBranchLeader() already:
         * - checks the selected member's branch
         * - finds the old leader
         * - demotes old leader to MEMBER
         * - promotes selected user to BRANCH_LEADER
         */
        if (
                requestedRole
                        == UserRole.BRANCH_LEADER
        ) {
            if (
                    member.getBranchId()
                            == null
            ) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Member must belong to a branch before becoming branch leader"
                );
            }

            branchService
                    .assignBranchLeader(
                            member.getBranchId(),
                            memberId
                    );

            return;
        }

        /*
         * =========================================
         * CURRENT BRANCH LEADER DEMOTION
         * =========================================
         *
         * Do not allow direct demotion here,
         * otherwise the branch may be left
         * without a leader.
         *
         * Replace the leader through the branch
         * assignment flow first.
         */
        if (
                targetUser.getRole()
                        == UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assign another branch leader before changing the current branch leader's role"
            );
        }

        /*
         * =========================================
         * NORMAL ROLES
         * =========================================
         *
         * MEMBER <-> SECRETARY
         *
         * Multiple secretaries are allowed,
         * so no replacement logic is necessary.
         */
        targetUser.setRole(
                requestedRole
        );

        userRepository
                .saveAndFlush(
                        targetUser
                );
    }

    private void validateAssignableRole(
            UserRole actorRole,
            UserRole requestedRole
    ) {
        if (actorRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        boolean allowed =
                switch (actorRole) {

                    case ADMIN ->
                            requestedRole == UserRole.MEMBER
                                    || requestedRole == UserRole.SECRETARY
                                    || requestedRole == UserRole.BRANCH_LEADER;

                    case BRANCH_LEADER ->
                            requestedRole == UserRole.MEMBER
                                    || requestedRole == UserRole.SECRETARY;

                    case SECRETARY ->
                            requestedRole == UserRole.MEMBER;

                    default -> false;
                };

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to assign role: "
                            + requestedRole
            );
        }
    }

    private User getCurrentUser() {
        User principal =
                SecurityUtil.getCurrentUser();

        if (
                principal == null
                        || principal.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return userRepository
                .findById(
                        principal.getId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );
    }
}