package org.example.tnal_youth_backend.member.password.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;
import org.example.tnal_youth_backend.member.password.exception.MemberPasswordException;
import org.example.tnal_youth_backend.member.password.service.MemberPasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPasswordServiceImpl
        implements MemberPasswordService {

    private final MemberRepository memberRepository;

    private final UserRepository userRepository;

    private final MemberAccessValidator
            memberAccessValidator;

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

        requireMember(memberId);

        return userRepository
                .findByMemberId(memberId)
                .map(this::toResponse)
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

        requireMember(memberId);

        User user =
                requireUserAccount(
                        memberId
                );

        if (user.getStatus()
                != UserStatus.PENDING_ACTIVATION) {

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
         * We still need to connect this to your existing OTP
         * service using purpose ACCOUNT_ACTIVATION.
         *
         * Do not silently return success before the OTP is
         * actually generated and delivered.
         */
        throw new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "Activation OTP delivery has not been connected yet"
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

        requireMember(memberId);

        User user =
                requireUserAccount(
                        memberId
                );

        /*
         * A member who has never activated their account
         * should complete activation instead of being disabled.
         */
        if (user.getStatus()
                == UserStatus.PENDING_ACTIVATION) {

            throw new MemberPasswordException(
                    "Pending activation accounts cannot be disabled."
            );
        }

        /*
         * Already inactive.
         */
        if (user.getStatus()
                == UserStatus.INACTIVE) {

            return toResponse(user);
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

        return toResponse(savedUser);
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

        requireMember(memberId);

        User user =
                requireUserAccount(
                        memberId
                );

        /*
         * Never allow staff to bypass first-time OTP activation.
         */
        if (user.getStatus()
                == UserStatus.PENDING_ACTIVATION) {

            throw new MemberPasswordException(
                    "This account is still pending activation. "
                            + "The member must verify OTP and set "
                            + "their first password."
            );
        }

        /*
         * activatedAt proves that the member completed
         * the first-time activation flow before.
         */
        if (user.getActivatedAt() == null) {
            throw new MemberPasswordException(
                    "This account has not completed OTP activation yet."
            );
        }

        /*
         * Already enabled.
         */
        if (user.getStatus()
                == UserStatus.ACTIVE) {

            return toResponse(user);
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

        return toResponse(savedUser);
    }

    /*
     * ==========================================================
     * FIND MEMBER
     * ==========================================================
     */

    private Member requireMember(
            Long memberId
    ) {
        if (memberId == null
                || memberId <= 0) {

            throw new MemberPasswordException(
                    "Member ID must be greater than zero"
            );
        }

        return memberRepository
                .findById(memberId)
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
                .findByMemberId(memberId)
                .orElseThrow(() ->
                        new MemberPasswordException(
                                "This member does not have "
                                        + "a login account"
                        )
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
        if (email == null
                || email.isBlank()) {

            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }
}