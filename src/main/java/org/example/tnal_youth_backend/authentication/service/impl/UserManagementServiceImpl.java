package org.example.tnal_youth_backend.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.model.enums.ViewerScope;
import org.example.tnal_youth_backend.authentication.model.request.CreateUserRequest;
import org.example.tnal_youth_backend.authentication.model.request.UpdateUserRequest;
import org.example.tnal_youth_backend.authentication.model.response.UserListItemResponse;
import org.example.tnal_youth_backend.authentication.model.response.UserSummaryResponse;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.service.UserManagementService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
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

    /** Roles whose standalone login must be scoped to one branch. */
    private static final Set<UserRole> BRANCH_SCOPED_ROLES =
            EnumSet.of(
                    UserRole.BRANCH_LEADER,
                    UserRole.SECRETARY,
                    UserRole.MEMBER
            );

    private final UserRepository userRepository;

    private final MemberRepository memberRepository;

    private final BranchRepository branchRepository;

    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // SUMMARY
    // =========================================================

    /*
     * The Users page represents login accounts, not only standalone
     * accounts. Therefore summary numbers count every row in users,
     * including accounts linked to members through member_id.
     *
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
        UserRole role = parseRole(request.getRole());
        ViewerScope viewerScope = validateViewerScope(role, request.getViewerScope());

        Long branchId = validateAndResolveBranchId(role, viewerScope, request.getBranchId());

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

        String requestedPassword = request.getPassword();

        String passwordHash =
                requestedPassword != null && !requestedPassword.isBlank()
                        ? passwordEncoder.encode(requestedPassword)
                        : passwordEncoder.encode(UUID.randomUUID().toString());

        User user = User.builder()
                .memberId(null)
                .branchId(branchId)
                .phone(phone)
                .email(email)
                .passwordHash(passwordHash)
                .role(role)
                .viewerScope(viewerScope)
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

    // =========================================================
    // UPDATE
    // =========================================================

    /*
     * Edits an existing standalone login account (member_id IS NULL).
     * A member-linked account is edited through the Member Page's own
     * personal-info flow instead — this deliberately refuses to touch
     * one, so the two edit paths never race or disagree about
     * validation rules for the same account.
     */
    @Override
    @Transactional
    public UserListItemResponse updateUser(
            Long id,
            UpdateUserRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with ID: " + id
                ));

        if (user.getMemberId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is linked to a member record — edit it "
                            + "from that member's personal-info page instead"
            );
        }

        UserRole role = parseRole(request.getRole());
        ViewerScope viewerScope = validateViewerScope(role, request.getViewerScope());

        Long branchId = validateAndResolveBranchId(role, viewerScope, request.getBranchId());

        String phone = request.getPhone().trim();

        if (userRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This phone number is already used by another account"
            );
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already used by another account"
            );
        }

        user.setFullNameKm(request.getFullNameKm().trim());
        user.setFullNameEn(
                request.getFullNameEn() != null
                        && !request.getFullNameEn().isBlank()
                        ? request.getFullNameEn().trim()
                        : null
        );
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setViewerScope(viewerScope);
        user.setBranchId(branchId);

        String requestedPassword = request.getPassword();

        if (requestedPassword != null && !requestedPassword.isBlank()) {
            user.setPasswordHash(
                    passwordEncoder.encode(requestedPassword)
            );
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(parseAdminSettableStatus(request.getStatus()));
        }

        User saved = userRepository.saveAndFlush(user);

        return toListItem(saved);
    }

    // Admin can only toggle between ACTIVE and INACTIVE — PENDING_ACTIVATION
    // (awaiting first OTP) and LOCKED (failed-login lockout) are system
    // states with their own flows, not something to hand-set here.
    private UserStatus parseAdminSettableStatus(String rawStatus) {
        UserStatus status;

        try {
            status = UserStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unknown status: " + rawStatus
            );
        }

        if (status != UserStatus.ACTIVE && status != UserStatus.INACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "status must be ACTIVE or INACTIVE"
            );
        }

        return status;
    }

    private UserRole parseRole(String rawRole) {
        try {
            return UserRole.valueOf(rawRole.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unknown role: " + rawRole
            );
        }
    }

    private ViewerScope validateViewerScope(UserRole role, String rawScope) {
        if (role != UserRole.VIEWER) {
            if (rawScope != null && !rawScope.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "viewerScope is only allowed when role is VIEWER");
            }
            return null;
        }
        if (rawScope == null || rawScope.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "viewerScope is required for VIEWER accounts");
        }
        try {
            return ViewerScope.valueOf(rawScope.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "viewerScope must be ADMIN, BRANCH_LEADER, SECRETARY, or MEMBER");
        }
    }

    private Long validateAndResolveBranchId(UserRole role, ViewerScope viewerScope, Long requestedBranchId) {
        boolean viewerNeedsBranch = role == UserRole.VIEWER && viewerScope != ViewerScope.ADMIN;
        if (BRANCH_SCOPED_ROLES.contains(role) || viewerNeedsBranch) {
            if (requestedBranchId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "branchId is required for MEMBER, SECRETARY, BRANCH_LEADER, and branch-scoped VIEWER accounts"
                );
            }
            if (!branchRepository.existsById(requestedBranchId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Branch " + requestedBranchId + " does not exist"
                );
            }
            return requestedBranchId;
        }

        // ADMIN and VIEWER-as-ADMIN are organization-wide.
        if (requestedBranchId != null && !branchRepository.existsById(requestedBranchId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch " + requestedBranchId + " does not exist"
            );
        }
        return requestedBranchId;
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
                .viewerScope(user.getViewerScope() != null ? user.getViewerScope().name() : null)
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
