package org.example.tnal_youth_backend.systemsettings.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
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

        return systemSettingsRepository
                .save(settings)
                .getDefaultMemberPassword();
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
