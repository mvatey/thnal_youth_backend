package org.example.tnal_youth_backend.security;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.ViewerScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ViewerAccessService {

    // Lazy: StaffBranchScopeService itself depends on this service.
    private final StaffBranchScopeService staffBranchScopeService;

    public ViewerAccessService(
            @Lazy StaffBranchScopeService staffBranchScopeService
    ) {
        this.staffBranchScopeService = staffBranchScopeService;
    }

    public boolean isViewer(User user) {
        return user != null && user.getRole() == UserRole.VIEWER;
    }

    /**
     * The role this account reads as. Staff of the country-level branch read
     * as an admin on every read request (so every list, detail and
     * dashboard they open is organization-wide, with the same code paths an
     * admin uses); on anything that changes data they stay a plain
     * secretary/branch leader limited to their own branch. Callers that
     * need the account's real staff role (StaffBranchScopeService) use
     * {@link #baseReadRole}.
     */
    public UserRole effectiveReadRole(User user) {
        if (staffBranchScopeService.isCountryStaffReading(user)) {
            return UserRole.ADMIN;
        }

        return baseReadRole(user);
    }

    public UserRole baseReadRole(User user) {
        if (user == null || user.getRole() == null) return null;
        if (user.getRole() != UserRole.VIEWER) return user.getRole();
        ViewerScope scope = user.getViewerScope();
        if (scope == null) return UserRole.ADMIN; // legacy VIEWER rows remain readable
        return UserRole.valueOf(scope.name());
    }

    public Long effectiveBranchId(User user) {
        return user == null ? null : user.getBranchId();
    }
}
