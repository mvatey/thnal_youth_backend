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
    private final BranchStaffRepository branchStaffRepository;

    public Member validateAccessibleMember(
            Long memberId
    ) {
        Member targetMember =
                findMember(memberId);

        validateBranchAccess(
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

        User principalUser =
                SecurityUtil.getCurrentUser();

        if (principalUser == null
                || principalUser.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        User currentUser =
                userRepository
                        .findById(principalUser.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found"
                                )
                        );

        UserRole role =
                currentUser.getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        /*
         * Admin can access members from any branch.
         */
        if (role == UserRole.ADMIN) {
            return;
        }

        /*
         * Only management roles may continue.
         */
        if (role != UserRole.SECRETARY
                && role != UserRole.BRANCH_LEADER) {

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
         * Temporary fallback for accounts without
         * branch_staff assignments.
         */
        if (accessibleBranchIds.isEmpty()) {
            Member currentMember =
                    findMember(currentMemberId);

            if (currentMember.getBranchId() != null) {
                accessibleBranchIds.add(
                        currentMember.getBranchId()
                );
            }
        }

        if (!accessibleBranchIds.contains(branchId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this member's branch"
            );
        }
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
}