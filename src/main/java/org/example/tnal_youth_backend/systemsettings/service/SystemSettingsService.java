package org.example.tnal_youth_backend.systemsettings.service;

public interface SystemSettingsService {

    /**
     * ADMIN-only, requires the requesting admin's own current password as
     * proof of identity (not the setting's own value -- there'd be no way
     * to recover from forgetting that). See SystemSettingsServiceImpl.
     */
    String revealDefaultMemberPassword(
            Long adminUserId,
            String adminCurrentPassword
    );

    String updateDefaultMemberPassword(
            Long adminUserId,
            String adminCurrentPassword,
            String newDefaultMemberPassword
    );

    /**
     * No auth check -- for internal use only (MemberServiceImpl reading the
     * current default when creating a new member-linked account).
     */
    String getDefaultMemberPasswordInternal();
}
