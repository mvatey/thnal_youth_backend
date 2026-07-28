package org.example.tnal_youth_backend.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.example.tnal_youth_backend.dashboard.exception.DashboardAccessException;
import org.example.tnal_youth_backend.dashboard.model.DashboardScope;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthRange;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthResolver;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DashboardScopeResolver {

    private final DashboardMonthResolver dashboardMonthResolver;
    private final MemberRepository memberRepository;
    private final BranchStaffRepository branchStaffRepository;

    public DashboardScope resolve(String month) {

        DashboardMonthRange monthRange =
                dashboardMonthResolver.resolve(month);

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new DashboardAccessException(
                    "Authentication is required to access the dashboard."
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal
                instanceof CustomUserDetails userDetails)) {

            throw new DashboardAccessException(
                    "Unable to resolve the authenticated user."
            );
        }

        User user = userDetails.getUser();
        UserRole role = user.getRole();

        if (role == null) {
            throw new DashboardAccessException(
                    "The authenticated user does not have a role."
            );
        }

        /*
         * ADMIN sees the whole organization.
         * No branch IDs need to be supplied.
         */
        if (role == UserRole.ADMIN) {
            return new DashboardScope(
                    user.getId(),
                    role,
                    Set.of(),
                    monthRange
            );
        }

        /*
         * SECRETARY and BRANCH_LEADER may be assigned
         * to one or more branches through branch_staff.
         */
        if (role == UserRole.SECRETARY
                || role == UserRole.BRANCH_LEADER) {

            Set<Long> branchIds =
                    resolveAccessibleBranchIds(user);

            return new DashboardScope(
                    user.getId(),
                    role,
                    branchIds,
                    monthRange
            );
        }

        throw new DashboardAccessException(
                "Your role does not have access to the dashboard."
        );
    }

    private Set<Long> resolveAccessibleBranchIds(
            User user
    ) {
        Long memberId = user.getMemberId();

        if (memberId == null) {
            throw new DashboardAccessException(
                    "This user account is not linked to a member record."
            );
        }

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new DashboardAccessException(
                                "The linked member record could not be found."
                        )
                );

        Set<Long> branchIds =
                new LinkedHashSet<>(
                        branchStaffRepository
                                .findActiveBranchIdsByMemberId(
                                        memberId
                                )
                );

        /*
         * Compatibility fallback:
         *
         * Older users may have members.branch_id populated
         * but no branch_staff assignment yet.
         */
        if (member.getBranchId() != null) {
            branchIds.add(
                    member.getBranchId()
            );
        }

        if (branchIds.isEmpty()) {
            throw new DashboardAccessException(
                    "The linked member does not have any active branch assignment."
            );
        }

        return Set.copyOf(branchIds);
    }
}