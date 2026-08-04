package org.example.tnal_youth_backend.dashboard.model;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthRange;

import java.util.Set;

public record DashboardScope(
        Long userId,
        UserRole role,
        Set<Long> accessibleBranchIds,
        DashboardMonthRange monthRange
) {

    public boolean organizationWide() {
        return role == UserRole.ADMIN;
    }

    public boolean hasBranchScope() {
        return !organizationWide()
                && accessibleBranchIds != null
                && !accessibleBranchIds.isEmpty();
    }

    public boolean canAccessBranch(Long branchId) {
        return organizationWide()
                || accessibleBranchIds.contains(branchId);
    }
}