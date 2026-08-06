package org.example.tnal_youth_backend.member.password.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.member.password.dto.request.MemberPasswordResetRequest;
import org.example.tnal_youth_backend.member.password.dto.request.UpdateMemberRoleRequest;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;
import org.example.tnal_youth_backend.member.password.exception.MemberPasswordException;
import org.example.tnal_youth_backend.member.password.service.MemberPasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPasswordServiceImpl
        implements MemberPasswordService {

    private final MemberRepository memberRepository;

    private final UserRepository userRepository;

    private final MemberAccessValidator
            memberAccessValidator;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenRepository
            refreshTokenRepository;

    /*
     * ==========================================================
     * GET ACCOUNT STATUS
     * ==========================================================
     */

    @Override
    public MemberPasswordStatusResponse getPasswordStatus(
            Long memberId
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        requireMember(
                memberId
        );

        return userRepository
                .findByMemberId(
                        memberId
                )
                .map(
                        this::toResponse
                )
                .orElseGet(() ->
                        noAccountResponse(
                                memberId
                        )
                );
    }

    /*
     * ==========================================================
     * RESEND ACTIVATION OTP
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberPasswordStatusResponse resendActivationOtp(
            Long memberId
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        requireMember(
                memberId
        );

        User user =
                requireUserAccount(
                        memberId
                );

        validateCanManageTargetAccount(
                user
        );

        if (
                user.getStatus()
                        != UserStatus.PENDING_ACTIVATION
        ) {
            throw new MemberPasswordException(
                    "Activation OTP can only be sent to an account "
                            + "that is pending activation"
            );
        }

        String email =
                normalizeEmail(
                        user.getEmail()
                );

        if (email == null) {
            throw new MemberPasswordException(
                    "The member account does not have an email "
                            + "for OTP delivery"
            );
        }

        /*
         * Connect this with the existing OTP service using
         * ACCOUNT_ACTIVATION as the OTP purpose.
         */
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Activation OTP delivery has not been connected yet"
        );
    }

    /*
     * ==========================================================
     * RESET PASSWORD
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberPasswordStatusResponse resetPassword(
            Long memberId,
            MemberPasswordResetRequest request
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        requireMember(
                memberId
        );

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password reset request is required"
            );
        }

        String newPassword =
                request.getNewPassword();

        String confirmPassword =
                request.getConfirmPassword();

        if (
                newPassword == null
                        || newPassword.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password is required"
            );
        }

        if (newPassword.length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 6 characters"
            );
        }

        if (
                confirmPassword == null
                        || confirmPassword.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password confirmation is required"
            );
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password confirmation does not match"
            );
        }

        User user =
                requireUserAccount(
                        memberId
                );

        validateCanManageTargetAccount(
                user
        );

        /*
         * Staff must not bypass first-time OTP activation.
         */
        if (
                user.getStatus()
                        == UserStatus.PENDING_ACTIVATION
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is pending activation. "
                            + "The member must complete OTP activation first."
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        newPassword
                )
        );

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        User savedUser =
                userRepository.saveAndFlush(
                        user
                );

        /*
         * Log out all current sessions after changing password.
         */
        revokeRefreshTokens(
                savedUser
        );

        return toResponse(
                savedUser
        );
    }

    /*
     * ==========================================================
     * UPDATE ACCOUNT ROLE
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberPasswordStatusResponse updateAccountRole(
            Long memberId,
            UpdateMemberRoleRequest request
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        requireMember(
                memberId
        );

        if (
                request == null
                        || request.role() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role is required"
            );
        }

        User targetUser =
                requireUserAccount(
                        memberId
                );

        UserRole actorRole =
                getCurrentActorRole();

        UserRole targetCurrentRole =
                targetUser.getRole();

        UserRole requestedRole =
                request.role();

        validateRoleChange(
                actorRole,
                targetCurrentRole,
                requestedRole
        );

        if (targetCurrentRole == requestedRole) {
            return toResponse(
                    targetUser
            );
        }

        targetUser.setRole(
                requestedRole
        );

        User savedUser =
                userRepository.saveAndFlush(
                        targetUser
                );

        /*
         * The user's permissions changed, so old sessions
         * should no longer remain active.
         */
        revokeRefreshTokens(
                savedUser
        );

        return toResponse(
                savedUser
        );
    }

    /*
     * ==========================================================
     * DISABLE ACCOUNT
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberPasswordStatusResponse disableAccount(
            Long memberId
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        requireMember(
                memberId
        );

        User user =
                requireUserAccount(
                        memberId
                );

        validateCanManageTargetAccount(
                user
        );

        if (
                user.getStatus()
                        == UserStatus.PENDING_ACTIVATION
        ) {
            throw new MemberPasswordException(
                    "Pending activation accounts cannot be disabled"
            );
        }

        if (
                user.getStatus()
                        == UserStatus.INACTIVE
        ) {
            return toResponse(
                    user
            );
        }

        user.setStatus(
                UserStatus.INACTIVE
        );

        user.setLockedUntil(null);
        user.setFailedLoginCount(0);

        User savedUser =
                userRepository.saveAndFlush(
                        user
                );

        /*
         * Immediately end existing sessions after disabling.
         */
        revokeRefreshTokens(
                savedUser
        );

        return toResponse(
                savedUser
        );
    }

    /*
     * ==========================================================
     * ENABLE ACCOUNT
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberPasswordStatusResponse enableAccount(
            Long memberId
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        requireMember(
                memberId
        );

        User user =
                requireUserAccount(
                        memberId
                );

        validateCanManageTargetAccount(
                user
        );

        /*
         * Staff cannot bypass first-time OTP activation.
         */
        if (
                user.getStatus()
                        == UserStatus.PENDING_ACTIVATION
        ) {
            throw new MemberPasswordException(
                    "This account is still pending activation. "
                            + "The member must verify OTP and set "
                            + "their first password."
            );
        }

        if (user.getActivatedAt() == null) {
            throw new MemberPasswordException(
                    "This account has not completed OTP activation yet"
            );
        }

        if (
                user.getStatus()
                        == UserStatus.ACTIVE
        ) {
            return toResponse(
                    user
            );
        }

        user.setStatus(
                UserStatus.ACTIVE
        );

        user.setLockedUntil(null);
        user.setFailedLoginCount(0);

        User savedUser =
                userRepository.saveAndFlush(
                        user
                );

        /*
         * Remove old sessions so the user signs in again
         * with the current account state.
         */
        revokeRefreshTokens(
                savedUser
        );

        return toResponse(
                savedUser
        );
    }

    /*
     * ==========================================================
     * ROLE VALIDATION
     * ==========================================================
     */

    private void validateRoleChange(
            UserRole actorRole,
            UserRole targetCurrentRole,
            UserRole requestedRole
    ) {
        if (actorRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account does not have a valid role"
            );
        }

        if (targetCurrentRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The target account does not have a role"
            );
        }

        if (requestedRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested role is required"
            );
        }

        /*
         * ADMIN must never be assigned through the member page.
         */
        if (requestedRole == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN role cannot be assigned from the member page"
            );
        }

        /*
         * Existing ADMIN accounts must not be managed through
         * the normal member-management page.
         */
        if (targetCurrentRole == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN accounts cannot be managed from the member page"
            );
        }

        /*
         * ADMIN:
         * - may manage MEMBER, SECRETARY, and BRANCH_LEADER
         * - may assign any of those three roles
         */
        if (actorRole == UserRole.ADMIN) {
            if (
                    requestedRole != UserRole.MEMBER
                            && requestedRole != UserRole.SECRETARY
                            && requestedRole
                            != UserRole.BRANCH_LEADER
            ) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Admin can assign MEMBER, SECRETARY, "
                                + "or BRANCH_LEADER only"
                );
            }

            return;
        }

        /*
         * BRANCH_LEADER:
         * - cannot manage another BRANCH_LEADER
         * - can manage MEMBER and SECRETARY only
         * - can assign MEMBER or SECRETARY
         *
         * Branch scope is already checked through
         * memberAccessValidator.
         */
        if (actorRole == UserRole.BRANCH_LEADER) {
            if (
                    targetCurrentRole
                            == UserRole.BRANCH_LEADER
            ) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Branch leader cannot manage another "
                                + "branch leader's role"
                );
            }

            if (
                    targetCurrentRole != UserRole.MEMBER
                            && targetCurrentRole
                            != UserRole.SECRETARY
            ) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Branch leader can only manage MEMBER "
                                + "or SECRETARY accounts"
                );
            }

            if (
                    requestedRole != UserRole.MEMBER
                            && requestedRole
                            != UserRole.SECRETARY
            ) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Branch leader can only assign MEMBER "
                                + "or SECRETARY"
                );
            }

            return;
        }

        /*
         * SECRETARY:
         * - can manage an existing MEMBER account only
         * - cannot promote the member
         * - therefore, the requested role must remain MEMBER
         *
         * Branch scope is already checked through
         * memberAccessValidator.
         */
        if (actorRole == UserRole.SECRETARY) {
            if (targetCurrentRole != UserRole.MEMBER) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Secretary can only manage MEMBER accounts"
                );
            }

            if (requestedRole != UserRole.MEMBER) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Secretary cannot promote or change "
                                + "a member's account role"
                );
            }

            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to change account roles"
        );
    }

    /*
     * ==========================================================
     * ACCOUNT MANAGEMENT VALIDATION
     * ==========================================================
     */

    private void validateCanManageTargetAccount(
            User targetUser
    ) {
        UserRole actorRole =
                getCurrentActorRole();

        UserRole targetRole =
                targetUser.getRole();

        if (targetRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The target account does not have a role"
            );
        }

        if (targetRole == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN accounts cannot be managed "
                            + "from the member page"
            );
        }

        /*
         * ADMIN can manage all non-admin member accounts.
         */
        if (actorRole == UserRole.ADMIN) {
            return;
        }

        /*
         * BRANCH_LEADER can manage MEMBER or SECRETARY
         * accounts inside their accessible branch scope.
         */
        if (actorRole == UserRole.BRANCH_LEADER) {
            if (
                    targetRole == UserRole.MEMBER
                            || targetRole == UserRole.SECRETARY
            ) {
                return;
            }

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Branch leader can only manage MEMBER "
                            + "or SECRETARY accounts"
            );
        }

        /*
         * SECRETARY can manage normal MEMBER accounts only.
         */
        if (actorRole == UserRole.SECRETARY) {
            if (targetRole == UserRole.MEMBER) {
                return;
            }

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Secretary can only manage MEMBER accounts"
            );
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to manage this account"
        );
    }

    /*
     * ==========================================================
     * CURRENT ACTOR ROLE
     * ==========================================================
     */

    private UserRole getCurrentActorRole() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        || !authentication.isAuthenticated()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        if (
                hasAuthority(
                        authentication,
                        "ROLE_ADMIN"
                )
        ) {
            return UserRole.ADMIN;
        }

        if (
                hasAuthority(
                        authentication,
                        "ROLE_BRANCH_LEADER"
                )
        ) {
            return UserRole.BRANCH_LEADER;
        }

        if (
                hasAuthority(
                        authentication,
                        "ROLE_SECRETARY"
                )
        ) {
            return UserRole.SECRETARY;
        }

        if (
                hasAuthority(
                        authentication,
                        "ROLE_MEMBER"
                )
        ) {
            return UserRole.MEMBER;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Authenticated user does not have a supported role"
        );
    }

    private boolean hasAuthority(
            Authentication authentication,
            String authority
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        authority.equals(
                                grantedAuthority.getAuthority()
                        )
                );
    }

    /*
     * ==========================================================
     * FIND MEMBER
     * ==========================================================
     */

    private Member requireMember(
            Long memberId
    ) {
        if (
                memberId == null
                        || memberId <= 0
        ) {
            throw new MemberPasswordException(
                    "Member ID must be greater than zero"
            );
        }

        return memberRepository
                .findById(
                        memberId
                )
                .orElseThrow(() ->
                        new MemberPasswordException(
                                "Member was not found with ID: "
                                        + memberId
                        )
                );
    }

    /*
     * ==========================================================
     * FIND USER ACCOUNT
     * ==========================================================
     */

    private User requireUserAccount(
            Long memberId
    ) {
        return userRepository
                .findByMemberId(
                        memberId
                )
                .orElseThrow(() ->
                        new MemberPasswordException(
                                "This member does not have "
                                        + "a login account"
                        )
                );
    }

    /*
     * ==========================================================
     * REFRESH TOKEN REVOCATION
     * ==========================================================
     */

    private void revokeRefreshTokens(
            User user
    ) {
        refreshTokenRepository
                .deleteByUser(
                        user
                );
    }

    /*
     * ==========================================================
     * RESPONSE MAPPING
     * ==========================================================
     */

    private MemberPasswordStatusResponse noAccountResponse(
            Long memberId
    ) {
        return new MemberPasswordStatusResponse(
                memberId,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private MemberPasswordStatusResponse toResponse(
            User user
    ) {
        return new MemberPasswordStatusResponse(
                user.getMemberId(),
                user.getId(),
                true,
                user.getActivatedAt() != null,
                user.getPhone(),
                user.getEmail(),

                user.getRole() != null
                        ? user.getRole().name()
                        : null,

                user.getStatus() != null
                        ? user.getStatus().name()
                        : null,

                user.getActivatedAt(),
                user.getLastLoginAt()
        );
    }

    /*
     * ==========================================================
     * NORMALIZATION
     * ==========================================================
     */

    private String normalizeEmail(
            String email
    ) {
        if (
                email == null
                        || email.isBlank()
        ) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}