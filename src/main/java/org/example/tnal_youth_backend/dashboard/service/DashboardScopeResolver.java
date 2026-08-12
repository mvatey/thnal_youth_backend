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

    public DashboardScope resolve(
            String month
    ) {
        DashboardMonthRange monthRange =
                dashboardMonthResolver.resolve(
                        month
                );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            throw new DashboardAccessException(
                    "Authentication is required to access the dashboard."
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (
                !(principal instanceof CustomUserDetails userDetails)
        ) {
            throw new DashboardAccessException(
                    "Unable to resolve the authenticated user."
            );
        }

        User user =
                userDetails.getUser();

        if (
                user == null
                        || user.getId() == null
        ) {
            throw new DashboardAccessException(
                    "Unable to resolve the authenticated user."
            );
        }

        UserRole role =
                user.getRole();

        if (role == null) {
            throw new DashboardAccessException(
                    "The authenticated user does not have a role."
            );
        }

        /*
         * =====================================================
         * ADMIN
         * =====================================================
         *
         * Admin is organization-wide.
         *
         * No branch IDs are stored in DashboardScope because
         * DashboardServiceImpl uses organization-wide queries
         * when organizationWide() is true.
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
         * =====================================================
         * SECRETARY / BRANCH LEADER
         * =====================================================
         *
         * Dashboard data is limited to their accessible
         * branch scope.
         */
        if (
                role == UserRole.SECRETARY
                        || role == UserRole.BRANCH_LEADER
        ) {
            Set<Long> accessibleBranchIds =
                    resolveAccessibleBranchIds(
                            user
                    );

            return new DashboardScope(
                    user.getId(),
                    role,
                    accessibleBranchIds,
                    monthRange
            );
        }

        /*
         * MEMBER does not have access to the main dashboard.
         */
        throw new DashboardAccessException(
                "Your role does not have access to the dashboard."
        );
    }

    private Set<Long> resolveAccessibleBranchIds(
            User user
    ) {
        Long memberId =
                user.getMemberId();

        if (memberId == null) {
            throw new DashboardAccessException(
                    "This user account is not linked to a member record."
            );
        }

        /*
         * Verify that the linked member exists.
         */
        Member member =
                memberRepository
                        .findById(
                                memberId
                        )
                        .orElseThrow(() ->
                                new DashboardAccessException(
                                        "The linked member record could not be found."
                                )
                        );

        /*
         * First priority:
         * active branch_staff assignments.
         */
        Set<Long> accessibleBranchIds =
                new LinkedHashSet<>(
                        branchStaffRepository
                                .findActiveBranchIdsByMemberId(
                                        memberId
                                )
                );

        /*
         * Fallback:
         *
         * Older/test data may have members.branch_id but no
         * active branch_staff record yet.
         *
         * Only use the primary branch when branch_staff
         * returned nothing.
         */
        if (
                accessibleBranchIds.isEmpty()
                        && member.getBranchId() != null
        ) {
            accessibleBranchIds.add(
                    member.getBranchId()
            );
        }

        if (accessibleBranchIds.isEmpty()) {
            throw new DashboardAccessException(
                    "The linked member does not have any active branch assignment."
            );
        }

        return Set.copyOf(
                accessibleBranchIds
        );
    }
}