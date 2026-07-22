package org.example.tnal_youth_backend.member.password.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.password.dto.request.ChangeMemberPasswordRequest;
import org.example.tnal_youth_backend.member.password.dto.request.CreateMemberPasswordRequest;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;
import org.example.tnal_youth_backend.member.password.exception.MemberPasswordException;
import org.example.tnal_youth_backend.member.password.service.MemberPasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPasswordServiceImpl
        implements MemberPasswordService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberPasswordStatusResponse getPasswordStatus(
            Long memberId
    ) {
        requireMember(memberId);

        return userRepository.findByMemberId(memberId)
                .map(this::toResponse)
                .orElseGet(() ->
                        new MemberPasswordStatusResponse(
                                memberId,
                                null,
                                false,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                );
    }

    @Override
    @Transactional
    public MemberPasswordStatusResponse createPassword(
            Long memberId,
            CreateMemberPasswordRequest request
    ) {
        Member member = requireMember(memberId);

        if (userRepository.existsByMemberId(memberId)) {
            throw new MemberPasswordException(
                    "This member already has a login account"
            );
        }

        validatePasswordConfirmation(
                request.newPassword(),
                request.confirmPassword()
        );

        validateMemberLoginInformation(member);
        validatePhoneAndEmailAvailability(member);

        User user = User.builder()
                .memberId(member.getId())
                .phone(member.getPhone().trim())
                .email(normalizeEmail(member.getEmail()))
                .passwordHash(
                        passwordEncoder.encode(
                                request.newPassword()
                        )
                )
                .role(UserRole.MEMBER)
                .status(UserStatus.ACTIVE)
                .fullNameKm(member.getFullNameKm())
                .fullNameEn(member.getFullNameEn())
                .profileImage(resolveProfileImage(member))
                .failedLoginCount(0)
                .build();

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Override
    @Transactional
    public MemberPasswordStatusResponse changePassword(
            Long memberId,
            ChangeMemberPasswordRequest request
    ) {
        requireMember(memberId);

        User user = userRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new MemberPasswordException(
                                "This member does not have a login account"
                        )
                );

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash()
        )) {
            throw new MemberPasswordException(
                    "Current password is incorrect"
            );
        }

        validatePasswordConfirmation(
                request.newPassword(),
                request.confirmPassword()
        );

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPasswordHash()
        )) {
            throw new MemberPasswordException(
                    "New password must be different from current password"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private Member requireMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new MemberPasswordException(
                                "Member was not found with id: "
                                        + memberId
                        )
                );
    }

    private void validatePasswordConfirmation(
            String password,
            String confirmPassword
    ) {
        if (!password.equals(confirmPassword)) {
            throw new MemberPasswordException(
                    "New password and confirm password do not match"
            );
        }
    }

    private void validateMemberLoginInformation(
            Member member
    ) {
        if (member.getPhone() == null
                || member.getPhone().isBlank()) {
            throw new MemberPasswordException(
                    "Member phone is required before creating a login account"
            );
        }
    }

    private void validatePhoneAndEmailAvailability(
            Member member
    ) {
        String phone = member.getPhone().trim();

        if (userRepository.existsByPhone(phone)) {
            throw new MemberPasswordException(
                    "Phone is already used by another login account"
            );
        }

        String email = normalizeEmail(member.getEmail());

        if (email != null
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new MemberPasswordException(
                    "Email is already used by another login account"
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    private String resolveProfileImage(Member member) {
        /*
         * Your FileEntity fields were not provided.
         * Return null for now.
         *
         * Later, if FileEntity has getFilePath(), use:
         *
         * return member.getProfilePhoto() == null
         *         ? null
         *         : member.getProfilePhoto().getFilePath();
         */
        return null;
    }

    private MemberPasswordStatusResponse toResponse(
            User user
    ) {
        return new MemberPasswordStatusResponse(
                user.getMemberId(),
                user.getId(),
                true,
                user.getPhone(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getUpdatedAt()
        );
    }
}