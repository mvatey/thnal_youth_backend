package org.example.tnal_youth_backend.security;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.ViewerScope;
import org.springframework.stereotype.Service;

@Service
public class ViewerAccessService {
    public boolean isViewer(User user) {
        return user != null && user.getRole() == UserRole.VIEWER;
    }

    public UserRole effectiveReadRole(User user) {
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
