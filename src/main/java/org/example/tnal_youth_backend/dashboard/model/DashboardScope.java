package org.example.tnal_youth_backend.dashboard.model;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.dashboard.util.DashboardMonthRange;

public record DashboardScope(

        Long userId,

        UserRole role,

        Long branchId,

        DashboardMonthRange monthRange

) {

    public boolean organizationWide() {
        return branchId == null;
    }

}