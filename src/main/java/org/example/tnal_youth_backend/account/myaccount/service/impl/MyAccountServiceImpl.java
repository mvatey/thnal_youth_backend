//package org.example.tnal_youth_backend.account.myaccount.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
//import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
//import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
//import org.example.tnal_youth_backend.account.myaccount.mapper.MyAccountMapper;
//import org.example.tnal_youth_backend.account.myaccount.service.MyAccountService;
//import org.example.tnal_youth_backend.authentication.model.entity.User;
//import org.example.tnal_youth_backend.authentication.repository.UserRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class MyAccountServiceImpl implements MyAccountService {
//
//    private static final Long INACTIVE_ACCOUNT_STATUS_ID = 2L;
//
//    /*
//     * Reuse UserRepository from Authentication.
//     * Do not create another repository inside My Account.
//     */
//    private final UserRepository userRepository;
//
//    private final PasswordEncoder passwordEncoder;
//
//    private final MyAccountMapper myAccountMapper;
//
//    /*
//     * ==========================================================
//     * GET MY ACCOUNT
//     * ==========================================================
//     */
//
//    @Override
//    public MyAccountResponse getMyAccount() {
//
//        User user = getCurrentUser();
//
//        return myAccountMapper.toResponse(user);
//    }
//
//    /*
//     * ==========================================================
//     * UPDATE MY ACCOUNT
//     * ==========================================================
//     */
//
//    @Override
//    @Transactional
//    public MyAccountResponse updateMyAccount(
//            UpdateMyAccountRequest request
//    ) {
//        User user = getCurrentUser();
//
//        String phone = normalizeValue(request.phone());
//        String email = normalizeEmail(request.email());
//
//        validatePhoneOrEmail(phone, email);
//
//        validatePhoneIsAvailable(
//                phone,
//                user.getId()
//        );
//
//        validateEmailIsAvailable(
//                email,
//                user.getId()
//        );
//
//        user.setPhone(phone);
//        user.setEmail(email);
//
//        User savedUser = userRepository.save(user);
//
//        return myAccountMapper.toResponse(savedUser);
//    }
//
//    /*
//     * ==========================================================
//     * CHANGE MY PASSWORD
//     * ==========================================================
//     */
//
//    @Override
//    @Transactional
//    public void changeMyPassword(
//            ChangeMyPasswordRequest request
//    ) {
//        User user = getCurrentUser();
//
//        validateCurrentPassword(
//                request.currentPassword(),
//                user.getPasswordHash()
//        );
//
//        validatePasswordConfirmation(
//                request.newPassword(),
//                request.confirmPassword()
//        );
//
//        validateNewPasswordIsDifferent(
//                request.newPassword(),
//                user.getPasswordHash()
//        );
//
//        user.setPasswordHash(
//                passwordEncoder.encode(
//                        request.newPassword()
//                )
//        );
//
//        user.setFailedLoginCount(0);
//        user.setLockedUntil(null);
//
//        userRepository.save(user);
//    }
//
//    /*
//     * ==========================================================
//     * DEACTIVATE MY ACCOUNT
//     * ==========================================================
//     */
//
//    @Override
//    @Transactional
//    public void deactivateMyAccount() {
//
//        User user = getCurrentUser();
//
//        /*
//         * Soft deactivation:
//         * update account_status_id instead of deleting the row.
//         *
//         * Change 2L if your INACTIVE status has another ID.
//         */
//        user.setAccountStatusId(
//                INACTIVE_ACCOUNT_STATUS_ID
//        );
//
//        user.setFailedLoginCount(0);
//        user.setLockedUntil(null);
//
//        userRepository.save(user);
//    }
//
//    /*
//     * ==========================================================
//     * GET AUTHENTICATED USER
//     * ==========================================================
//     */
//
//    private User getCurrentUser() {
//
//        Authentication authentication =
//                SecurityContextHolder
//                        .getContext()
//                        .getAuthentication();
//
//        if (authentication == null
//                || !authentication.isAuthenticated()
//                || "anonymousUser".equals(
//                authentication.getPrincipal()
//        )) {
//            throw new IllegalStateException(
//                    "User is not authenticated"
//            );
//        }
//
//        /*
//         * Spring Security username returned by your
//         * UserDetails implementation.
//         *
//         * It should be either email or phone.
//         */
//        String loginIdentifier =
//                authentication.getName();
//
//        if (loginIdentifier == null
//                || loginIdentifier.isBlank()) {
//            throw new IllegalStateException(
//                    "Authenticated login identifier was not found"
//            );
//        }
//
//        return userRepository
//                .findByEmailIgnoreCase(loginIdentifier)
//                .or(() ->
//                        userRepository.findByPhone(
//                                loginIdentifier
//                        )
//                )
//                .orElseThrow(() ->
//                        new IllegalStateException(
//                                "Authenticated user was not found"
//                        )
//                );
//    }
//
//    /*
//     * ==========================================================
//     * PHONE AND EMAIL VALIDATION
//     * ==========================================================
//     */
//
//    private void validatePhoneOrEmail(
//            String phone,
//            String email
//    ) {
//        if (phone == null && email == null) {
//            throw new IllegalArgumentException(
//                    "At least one phone number or email is required"
//            );
//        }
//    }
//
//    private void validatePhoneIsAvailable(
//            String phone,
//            Long currentUserId
//    ) {
//        if (phone == null) {
//            return;
//        }
//
//        boolean exists =
//                userRepository.existsByPhoneAndIdNot(
//                        phone,
//                        currentUserId
//                );
//
//        if (exists) {
//            throw new IllegalArgumentException(
//                    "Phone number is already being used"
//            );
//        }
//    }
//
//    private void validateEmailIsAvailable(
//            String email,
//            Long currentUserId
//    ) {
//        if (email == null) {
//            return;
//        }
//
//        boolean exists =
//                userRepository
//                        .existsByEmailIgnoreCaseAndIdNot(
//                                email,
//                                currentUserId
//                        );
//
//        if (exists) {
//            throw new IllegalArgumentException(
//                    "Email is already being used"
//            );
//        }
//    }
//
//    /*
//     * ==========================================================
//     * PASSWORD VALIDATION
//     * ==========================================================
//     */
//
//    private void validateCurrentPassword(
//            String currentPassword,
//            String currentPasswordHash
//    ) {
//        boolean matches =
//                passwordEncoder.matches(
//                        currentPassword,
//                        currentPasswordHash
//                );
//
//        if (!matches) {
//            throw new IllegalArgumentException(
//                    "Current password is incorrect"
//            );
//        }
//    }
//
//    private void validatePasswordConfirmation(
//            String newPassword,
//            String confirmPassword
//    ) {
//        if (!newPassword.equals(confirmPassword)) {
//            throw new IllegalArgumentException(
//                    "New password and confirmation do not match"
//            );
//        }
//    }
//
//    private void validateNewPasswordIsDifferent(
//            String newPassword,
//            String currentPasswordHash
//    ) {
//        boolean samePassword =
//                passwordEncoder.matches(
//                        newPassword,
//                        currentPasswordHash
//                );
//
//        if (samePassword) {
//            throw new IllegalArgumentException(
//                    "New password must be different from the current password"
//            );
//        }
//    }
//
//    /*
//     * ==========================================================
//     * NORMALIZATION
//     * ==========================================================
//     */
//
//    private String normalizeValue(String value) {
//
//        if (value == null || value.isBlank()) {
//            return null;
//        }
//
//        return value.trim();
//    }
//
//    private String normalizeEmail(String email) {
//
//        String normalizedEmail =
//                normalizeValue(email);
//
//        if (normalizedEmail == null) {
//            return null;
//        }
//
//        return normalizedEmail.toLowerCase();
//    }
//}