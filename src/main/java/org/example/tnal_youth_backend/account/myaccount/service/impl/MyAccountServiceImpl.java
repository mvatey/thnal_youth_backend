package org.example.tnal_youth_backend.account.myaccount.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.account.myaccount.mapper.MyAccountMapper;
import org.example.tnal_youth_backend.account.myaccount.service.MyAccountService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyAccountServiceImpl implements MyAccountService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;

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
        Member member = getLinkedMember(user);

        return myAccountMapper.toResponse(
                user,
                member
        );
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
        Member member = getLinkedMember(user);

        String phone = normalize(request.phone());
        String email = normalizeEmail(request.email());
        String fullNameKm = normalize(request.fullNameKm());
        String fullNameEn = normalize(request.fullNameEn());

        validateRequiredPhone(phone);
        validateRequiredKhmerName(fullNameKm);

        validatePhoneIsAvailable(
                phone,
                user,
                member
        );

        validateEmailIsAvailable(
                email,
                user,
                member
        );

        /*
         * Update members.
         *
         * Member Page reads this same row, so it will immediately
         * see the changes made from My Account.
         */
        member.setPhone(phone);
        member.setEmail(email);
        member.setFullNameKm(fullNameKm);
        member.setFullNameEn(fullNameEn);
        member.setGender(request.gender());
        member.setDateOfBirth(request.dateOfBirth());
        member.setPlaceOfBirth(
                normalize(request.placeOfBirth())
        );
        member.setCurrentAddress(
                normalize(request.currentAddress())
        );
        member.setPermanentAddress(
                normalize(request.permanentAddress())
        );
        member.setBio(
                normalize(request.bio())
        );

        updateProfilePhoto(
                member,
                request.profilePhotoId()
        );

        /*
         * Keep authentication fields synchronized.
         *
         * Login uses users.phone/users.email.
         * Sidebar and older authentication responses may still use
         * the name and profile image stored in users.
         */
        user.setPhone(phone);
        user.setEmail(email);
        user.setFullNameKm(fullNameKm);
        user.setFullNameEn(fullNameEn);

        if (member.getProfilePhoto() != null) {
            user.setProfileImage(
                    member.getProfilePhoto().getFilePath()
            );
        }

        Member savedMember = memberRepository.save(member);
        User savedUser = userRepository.save(user);

        return myAccountMapper.toResponse(
                savedUser,
                savedMember
        );
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
     * CURRENT AUTHENTICATED USER
     * ==========================================================
     */

    private User getCurrentUserFromDatabase() {

        User authenticatedUser =
                SecurityUtil.getCurrentUser();

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
     * LINKED MEMBER
     * ==========================================================
     */

    private Member getLinkedMember(User user) {

        if (user.getMemberId() == null) {
            throw new IllegalStateException(
                    "This account is not linked to a member profile"
            );
        }

        return memberRepository
                .findDetailedById(user.getMemberId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "The member profile linked to this account "
                                        + "was not found"
                        )
                );
    }

    /*
     * ==========================================================
     * PROFILE PHOTO
     * ==========================================================
     */

    private void updateProfilePhoto(
            Member member,
            Long profilePhotoId
    ) {
        /*
         * Null means that the current profile photo is unchanged.
         */
        if (profilePhotoId == null) {
            return;
        }

        FileEntity profilePhoto =
                fileRepository
                        .findById(profilePhotoId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Profile photo file was not found"
                                )
                        );

        validateProfilePhoto(profilePhoto);

        member.setProfilePhoto(profilePhoto);
    }

    private void validateProfilePhoto(
            FileEntity profilePhoto
    ) {
        String mimeType = profilePhoto.getMimeType();

        if (mimeType == null
                || !mimeType.toLowerCase(Locale.ROOT)
                .startsWith("image/")) {
            throw new IllegalArgumentException(
                    "The selected file must be an image"
            );
        }
    }

    /*
     * ==========================================================
     * REQUIRED FIELD VALIDATION
     * ==========================================================
     */

    private void validateRequiredPhone(String phone) {

        if (phone == null) {
            throw new IllegalArgumentException(
                    "Phone number is required"
            );
        }
    }

    private void validateRequiredKhmerName(
            String fullNameKm
    ) {
        if (fullNameKm == null) {
            throw new IllegalArgumentException(
                    "Khmer full name is required"
            );
        }
    }

    /*
     * ==========================================================
     * PHONE VALIDATION
     * ==========================================================
     */

    private void validatePhoneIsAvailable(
            String phone,
            User currentUser,
            Member currentMember
    ) {
        boolean usedByAnotherUser =
                userRepository.existsByPhoneAndIdNot(
                        phone,
                        currentUser.getId()
                );

        if (usedByAnotherUser) {
            throw new IllegalArgumentException(
                    "Phone number is already used by another account"
            );
        }

        boolean usedByAnotherMember =
                memberRepository.existsByPhoneAndIdNot(
                        phone,
                        currentMember.getId()
                );

        if (usedByAnotherMember) {
            throw new IllegalArgumentException(
                    "Phone number is already used by another member"
            );
        }
    }

    /*
     * ==========================================================
     * EMAIL VALIDATION
     * ==========================================================
     */

    private void validateEmailIsAvailable(
            String email,
            User currentUser,
            Member currentMember
    ) {
        if (email == null) {
            return;
        }

        boolean usedByAnotherUser =
                userRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                email,
                                currentUser.getId()
                        );

        if (usedByAnotherUser) {
            throw new IllegalArgumentException(
                    "Email is already used by another account"
            );
        }

        boolean usedByAnotherMember =
                memberRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                email,
                                currentMember.getId()
                        );

        if (usedByAnotherMember) {
            throw new IllegalArgumentException(
                    "Email is already used by another member"
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
        if (currentPasswordHash == null
                || !passwordEncoder.matches(
                currentPassword,
                currentPasswordHash
        )) {
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
        if (currentPasswordHash != null
                && passwordEncoder.matches(
                newPassword,
                currentPasswordHash
        )) {
            throw new IllegalArgumentException(
                    "New password must be different "
                            + "from the current password"
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

        return normalizedEmail.toLowerCase(
                Locale.ROOT
        );
    }
}