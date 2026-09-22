package org.example.tnal_youth_backend.systemsettings.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.common.validation.PasswordPolicy;
import org.example.tnal_youth_backend.systemsettings.entity.SystemSettings;
import org.example.tnal_youth_backend.systemsettings.repository.SystemSettingsRepository;
import org.example.tnal_youth_backend.systemsettings.service.SystemSettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private final SystemSettingsRepository systemSettingsRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public String revealDefaultMemberPassword(
            Long adminUserId,
            String adminCurrentPassword
    ) {
        requireVerifiedAdmin(adminUserId, adminCurrentPassword);
        return getSettings().getDefaultMemberPassword();
    }

    @Override
    @Transactional
    public String updateDefaultMemberPassword(
            Long adminUserId,
            String adminCurrentPassword,
            String newDefaultMemberPassword
    ) {
        requireVerifiedAdmin(adminUserId, adminCurrentPassword);

        if (!PasswordPolicy.isValid(newDefaultMemberPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    PasswordPolicy.MESSAGE
            );
        }

        SystemSettings settings = getSettings();
        settings.setDefaultMemberPassword(newDefaultMemberPassword);
        settings.setUpdatedBy(adminUserId);

        String saved = systemSettingsRepository
                .save(settings)
                .getDefaultMemberPassword();

        resyncPendingAccountsToNewDefault(newDefaultMemberPassword);

        return saved;
    }

    /*
     * Every account still on mustChangePassword=true is, by definition,
     * still sitting on WHATEVER the default was when it was created --
     * without this, changing the setting only ever affected accounts
     * created afterward, silently leaving older still-pending accounts on
     * a stale value nobody could log in with anymore (since only the
     * *current* setting is shown/reveal-able going forward). An account
     * that already has its own real password (mustChangePassword=false)
     * is never touched here.
     */
    private void resyncPendingAccountsToNewDefault(
            String newDefaultMemberPassword
    ) {
        List<User> pendingUsers = userRepository
                .findByMustChangePasswordTrueAndStatus(UserStatus.ACTIVE);

        if (pendingUsers.isEmpty()) {
            return;
        }

        String newHash = passwordEncoder.encode(newDefaultMemberPassword);

        pendingUsers.forEach(user -> user.setPasswordHash(newHash));

        userRepository.saveAll(pendingUsers);
    }

    @Override
    @Transactional(readOnly = true)
    public String getDefaultMemberPasswordInternal() {
        return getSettings().getDefaultMemberPassword();
    }

    /*
     * Both reveal and update are gated on the REQUESTING admin's own
     * current login password, not the setting's own value -- forgetting a
     * shared system setting nobody actually memorizes would otherwise be
     * an unrecoverable lockout. This works the same regardless of how many
     * admin accounts exist, since each admin re-proves their own identity.
     */
    private User requireVerifiedAdmin(
            Long adminUserId,
            String adminCurrentPassword
    ) {
        User admin = userRepository
                .findById(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid session"
                ));

        if (adminCurrentPassword == null || adminCurrentPassword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Your current password is required"
            );
        }

        if (!passwordEncoder.matches(adminCurrentPassword, admin.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Your current password is incorrect"
            );
        }

        return admin;
    }

    private SystemSettings getSettings() {
        return systemSettingsRepository
                .findById((short) 1)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "System settings are missing"
                ));
    }
}
