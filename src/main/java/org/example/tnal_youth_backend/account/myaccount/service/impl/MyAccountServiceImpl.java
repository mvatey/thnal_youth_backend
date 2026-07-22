package org.example.tnal_youth_backend.account.myaccount.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.account.myaccount.mapper.MyAccountMapper;
import org.example.tnal_youth_backend.account.myaccount.service.MyAccountService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyAccountServiceImpl implements MyAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MyAccountMapper myAccountMapper;

    /*
     * ==========================================================
     * GET MY ACCOUNT
     * ==========================================================
     */

    @Override
    public MyAccountResponse getMyAccount() {

        User user = getCurrentUserFromDatabase();

        return myAccountMapper.toResponse(user);
    }

    /*
     * ==========================================================
     * UPDATE MY ACCOUNT
     * ==========================================================
     */

    @Override
    @Transactional
    public MyAccountResponse updateMyAccount(
            UpdateMyAccountRequest request
    ) {
        User user = getCurrentUserFromDatabase();

        String phone = normalize(request.phone());
        String email = normalizeEmail(request.email());
        String fullNameKm = normalize(request.fullNameKm());
        String fullNameEn = normalize(request.fullNameEn());
        String profileImage = normalize(request.profileImage());

        validatePhoneOrEmail(phone, email);

        validatePhoneIsAvailable(
                phone,
                user.getId()
        );

        validateEmailIsAvailable(
                email,
                user.getId()
        );

        user.setPhone(phone);
        user.setEmail(email);
        user.setFullNameKm(fullNameKm);
        user.setFullNameEn(fullNameEn);
        user.setProfileImage(profileImage);

        User savedUser = userRepository.save(user);

        return myAccountMapper.toResponse(savedUser);
    }

    /*
     * ==========================================================
     * CHANGE PASSWORD
     * ==========================================================
     */

    @Override
    @Transactional
    public void changeMyPassword(
            ChangeMyPasswordRequest request
    ) {
        User user = getCurrentUserFromDatabase();

        validateCurrentPassword(
                request.currentPassword(),
                user.getPasswordHash()
        );

        validatePasswordConfirmation(
                request.newPassword(),
                request.confirmPassword()
        );

        validateNewPasswordIsDifferent(
                request.newPassword(),
                user.getPasswordHash()
        );

        String encodedPassword =
                passwordEncoder.encode(
                        request.newPassword()
                );

        user.setPasswordHash(encodedPassword);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        userRepository.save(user);
    }

    /*
     * ==========================================================
     * DEACTIVATE MY ACCOUNT
     * ==========================================================
     */

    @Override
    @Transactional
    public void deactivateMyAccount() {

        User user = getCurrentUserFromDatabase();

        /*
         * Soft deactivation.
         * Do not permanently delete the users row.
         */
        user.setStatus(UserStatus.INACTIVE);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        userRepository.save(user);
    }

    /*
     * ==========================================================
     * CURRENT AUTHENTICATED USER
     * ==========================================================
     */

    private User getCurrentUserFromDatabase() {

        User authenticatedUser = SecurityUtil.getCurrentUser();

        if (authenticatedUser == null
                || authenticatedUser.getId() == null) {
            throw new IllegalStateException(
                    "Authenticated user was not found"
            );
        }

        return userRepository
                .findById(authenticatedUser.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user was not found in database"
                        )
                );
    }

    /*
     * ==========================================================
     * PHONE AND EMAIL VALIDATION
     * ==========================================================
     */

    private void validatePhoneOrEmail(
            String phone,
            String email
    ) {
        if (phone == null && email == null) {
            throw new IllegalArgumentException(
                    "At least one phone number or email is required"
            );
        }
    }

    private void validatePhoneIsAvailable(
            String phone,
            Long currentUserId
    ) {
        if (phone == null) {
            return;
        }

        boolean phoneExists =
                userRepository.existsByPhoneAndIdNot(
                        phone,
                        currentUserId
                );

        if (phoneExists) {
            throw new IllegalArgumentException(
                    "Phone number is already being used"
            );
        }
    }

    private void validateEmailIsAvailable(
            String email,
            Long currentUserId
    ) {
        if (email == null) {
            return;
        }

        boolean emailExists =
                userRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                email,
                                currentUserId
                        );

        if (emailExists) {
            throw new IllegalArgumentException(
                    "Email is already being used"
            );
        }
    }

    /*
     * ==========================================================
     * PASSWORD VALIDATION
     * ==========================================================
     */

    private void validateCurrentPassword(
            String currentPassword,
            String currentPasswordHash
    ) {
        boolean passwordMatches =
                passwordEncoder.matches(
                        currentPassword,
                        currentPasswordHash
                );

        if (!passwordMatches) {
            throw new IllegalArgumentException(
                    "Current password is incorrect"
            );
        }
    }

    private void validatePasswordConfirmation(
            String newPassword,
            String confirmPassword
    ) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "New password and confirmation do not match"
            );
        }
    }

    private void validateNewPasswordIsDifferent(
            String newPassword,
            String currentPasswordHash
    ) {
        boolean sameAsCurrentPassword =
                passwordEncoder.matches(
                        newPassword,
                        currentPasswordHash
                );

        if (sameAsCurrentPassword) {
            throw new IllegalArgumentException(
                    "New password must be different from the current password"
            );
        }
    }

    /*
     * ==========================================================
     * NORMALIZATION
     * ==========================================================
     */

    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(String email) {

        String normalizedEmail = normalize(email);

        if (normalizedEmail == null) {
            return null;
        }

        return normalizedEmail.toLowerCase(Locale.ROOT);
    }
}