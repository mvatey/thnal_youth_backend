package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.model.request.CreateUserRequest;
import org.example.tnal_youth_backend.authentication.model.response.UserListItemResponse;
import org.example.tnal_youth_backend.authentication.model.response.UserSummaryResponse;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.service.UserManagementService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * Official implementation for administrative user-account
 * management under /api/admin/users.
 */
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl
        implements UserManagementService {

    /*
     * Roles that can be created through this branch-less,
     * admin-only account flow. MEMBER, BRANCH_LEADER, and
     * SECRETARY accounts are always tied to a member/branch
     * and must be created through those existing flows instead.
     */
    private static final Set<UserRole> STANDALONE_CREATABLE_ROLES =
            EnumSet.of(
                    UserRole.ADMIN,
                    UserRole.VIEWER
            );

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // SUMMARY
    // =========================================================

    /*
     * The Users page represents login accounts, not only standalone
     * accounts. Therefore summary numbers count every row in users,
     * including accounts linked to members through member_id.
     *
     * STANDALONE_CREATABLE_ROLES is intentionally used only by the
     * create flow; it must never be used to hide linked accounts from
     * the Users list or summary.
     */
    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getSummary() {
        long total = userRepository.count();

        long active = userRepository
                .countByStatus(UserStatus.ACTIVE);

        long inactive = Math.max(
                total - active,
                0
        );

        return UserSummaryResponse.builder()
                .totalUsers(total)
                .activeUsers(active)
                .inactiveUsers(inactive)
                .build();
    }

    // =========================================================
    // LIST
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserListItemResponse> listUsers(
            String search,
            String role,
            String status
    ) {
        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim().toLowerCase();

        String normalizedRole =
                (role == null || role.isBlank() || "ALL".equalsIgnoreCase(role))
                        ? null
                        : role.trim().toUpperCase();

        String normalizedStatus =
                (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status))
                        ? null
                        : status.trim().toUpperCase();

        return userRepository
                .findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .filter(user ->
                        normalizedRole == null
                                || (user.getRole() != null
                                && user.getRole().name().equals(normalizedRole))
                )
                .filter(user ->
                        normalizedStatus == null
                                || matchesVisibleStatus(user, normalizedStatus)
                )
                .filter(user ->
                        normalizedSearch.isEmpty()
                                || matchesSearch(user, normalizedSearch)
                )
                .map(this::toListItem)
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(
            User user,
            String normalizedSearch
    ) {
        return containsIgnoreCase(user.getFullNameKm(), normalizedSearch)
                || containsIgnoreCase(user.getFullNameEn(), normalizedSearch)
                || containsIgnoreCase(user.getPhone(), normalizedSearch)
                || containsIgnoreCase(user.getEmail(), normalizedSearch);
    }

    private boolean containsIgnoreCase(
            String value,
            String normalizedSearch
    ) {
        return value != null
                && value.toLowerCase().contains(normalizedSearch);
    }

    /*
     * The visible status on the Users page follows Member status for
     * member-linked accounts, so it stays consistent with Member Detail
     * and the Member list. Standalone accounts have no Member row, so
     * their internal users.status is used as the fallback.
     */
    private boolean matchesVisibleStatus(
            User user,
            String normalizedStatus
    ) {
        if (user.getMemberId() != null) {
            return memberRepository.findById(user.getMemberId())
                    .map(Member::getStatus)
                    .map(status -> status.getCode() != null
                            && status.getCode().equalsIgnoreCase(normalizedStatus))
                    .orElse(false);
        }

        return user.getStatus() != null
                && user.getStatus().name().equalsIgnoreCase(normalizedStatus);
    }

    // =========================================================
    // CREATE
    // =========================================================

    /*
     * No password is collected here. This mirrors how
     * MemberServiceImpl provisions member/branch-staff logins:
     * the account is created PENDING_ACTIVATION with an unusable
     * placeholder password hash, and the new user sets their own
     * first password through the existing OTP activation flow
     * (send-otp -> verify-otp -> set-password), which is why an
     * email is required — that's the OTP delivery channel.
     */
    @Override
    @Transactional
    public UserListItemResponse createUser(
            CreateUserRequest request
    ) {
        UserRole role = parseCreatableRole(request.getRole());

        String phone = request.getPhone().trim();

        if (userRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This phone number is already used by another account"
            );
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already used by another account"
            );
        }

        String unusablePasswordHash =
                passwordEncoder.encode(
                        UUID.randomUUID().toString()
                );

        User user = User.builder()
                .memberId(null)
                .phone(phone)
                .email(email)
                .passwordHash(unusablePasswordHash)
                .role(role)
                .status(UserStatus.PENDING_ACTIVATION)
                .fullNameKm(request.getFullNameKm().trim())
                .fullNameEn(
                        request.getFullNameEn() != null
                                && !request.getFullNameEn().isBlank()
                                ? request.getFullNameEn().trim()
                                : null
                )
                .failedLoginCount(0)
                .build();

        User saved = userRepository.saveAndFlush(user);

        return toListItem(saved);
    }

    private UserRole parseCreatableRole(String rawRole) {
        UserRole role;

        try {
            role = UserRole.valueOf(
                    rawRole.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unknown role: " + rawRole
            );
        }

        if (!STANDALONE_CREATABLE_ROLES.contains(role)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This endpoint can only create ADMIN or VIEWER accounts. "
                            + "Member, branch leader, and secretary accounts must be "
                            + "created through the member/branch management flow."
            );
        }

        return role;
    }

    // =========================================================
    // MAPPING
    // =========================================================

    private UserListItemResponse toListItem(User user) {
        Member linkedMember =
                user.getMemberId() == null
                        ? null
                        : memberRepository.findById(user.getMemberId()).orElse(null);

        return UserListItemResponse.builder()
                .id(user.getId())
                .memberId(user.getMemberId())
                .branchId(user.getBranchId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .fullNameKm(user.getFullNameKm())
                .fullNameEn(user.getFullNameEn())
                .profileImage(user.getProfileImage())
                .role(
                        user.getRole() != null
                                ? user.getRole().name()
                                : null
                )
                .status(
                        user.getStatus() != null
                                ? user.getStatus().name()
                                : null
                )
                .memberStatusId(
                        linkedMember != null && linkedMember.getStatus() != null
                                ? linkedMember.getStatus().getId()
                                : null
                )
                .memberStatusCode(
                        linkedMember != null && linkedMember.getStatus() != null
                                ? linkedMember.getStatus().getCode()
                                : null
                )
                .memberStatusLabelKm(
                        linkedMember != null && linkedMember.getStatus() != null
                                ? linkedMember.getStatus().getLabelKm()
                                : null
                )
                .memberStatusLabelEn(
                        linkedMember != null && linkedMember.getStatus() != null
                                ? linkedMember.getStatus().getLabelEn()
                                : null
                )
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
