package org.example.tnal_youth_backend.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.example.tnal_youth_backend.dashboard.exception.DashboardAccessException;
import org.example.tnal_youth_backend.dashboard.model.DashboardScope;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthRange;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthResolver;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardScopeResolver {

    private final DashboardMonthResolver dashboardMonthResolver;
    private final MemberRepository memberRepository;

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

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
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

        if (role == UserRole.ADMIN) {
            return new DashboardScope(
                    user.getId(),
                    role,
                    null,
                    monthRange
            );
        }

        if (role == UserRole.SECRETARY
                || role == UserRole.BRANCH_LEADER) {

            Long branchId =
                    resolveAssignedBranchId(user);

            return new DashboardScope(
                    user.getId(),
                    role,
                    branchId,
                    monthRange
            );
        }

        throw new DashboardAccessException(
                "Your role does not have access to the dashboard."
        );
    }

    private Long resolveAssignedBranchId(User user) {

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

        Long branchId = member.getBranchId();

        if (branchId == null) {
            throw new DashboardAccessException(
                    "The linked member does not have an assigned branch."
            );
        }

        return branchId;
    }
}