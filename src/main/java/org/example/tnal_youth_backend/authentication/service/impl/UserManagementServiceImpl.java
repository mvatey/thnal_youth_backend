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
 * Given an explicit bean name — see the note in
 * UserManagementController.java for why this is necessary
 * (a pre-existing, unrelated UserManagementServiceImpl already
 * exists under org.example.tnal_youth_backend.account.user).
 */
@Service("adminUserManagementServiceImpl")
@RequiredArgsConstructor
public class UserManagementServiceImpl
        implements UserManagementService {

    /*
     * Roles that can be created through this branch-less,
     * admin-only account flow. MEMBER, BRANCH_LEADER, and
     * SECRETARY accounts are always tied to a member/branch
     * and must be created through those existing flows instead.
     */
    private static final Set<UserRole> CREATABLE_ROLES =
            EnumSet.of(
                    UserRole.ADMIN,
                    UserRole.VIEWER
            );

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // SUMMARY
    // =========================================================

    /*
     * "Users" here means accounts whose role is not tied to a
     * branch or member at all — i.e. CREATABLE_ROLES (ADMIN,
     * VIEWER). Those already have their own home in the Members
     * page; this summary/list only ever counts CREATABLE_ROLES
     * accounts managed here.
     *
     * This used to filter on memberId IS NULL instead, but some
     * legacy/seed BRANCH_LEADER accounts were created directly via
     * SQL migration with member_id left NULL (see
     * V324__activate_branch_staff_login_accounts.sql), so they
     * slipped through — role is the reliable filter.
     */
    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getSummary() {
        long total = userRepository.countByRoleIn(CREATABLE_ROLES);

        long active = userRepository
                .countByRoleInAndStatus(CREATABLE_ROLES, UserStatus.ACTIVE);

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
                .findAllByRoleInOrderByCreatedAtDescIdDesc(CREATABLE_ROLES)
                .stream()
                .filter(user ->
                        normalizedRole == null
                                || (user.getRole() != null
                                && user.getRole().name().equals(normalizedRole))
                )
                .filter(user ->
                        normalizedStatus == null
                                || (user.getStatus() != null
                                && user.getStatus().name().equals(normalizedStatus))
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

        if (!CREATABLE_ROLES.contains(role)) {
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
        return UserListItemResponse.builder()
                .id(user.getId())
                .memberId(user.getMemberId())
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
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
