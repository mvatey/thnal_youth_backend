package org.example.tnal_youth_backend.security;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserBranchAssignmentRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.position.entity.Position;
import org.example.tnal_youth_backend.member.position.repository.PositionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Single source of truth for branch-scoped staff authorization.
 *
 * SECRETARY: home branch + every active branch_staff assignment.
 * BRANCH_LEADER: exactly one branch only (users.branch_id, falling back to members.branch_id).
 *
 * ADMIN/VIEWER/MEMBER are intentionally not resolved here; callers keep their
 * existing role-specific behavior.
 */
@Service
@RequiredArgsConstructor
public class StaffBranchScopeService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final BranchStaffRepository branchStaffRepository;
    private final UserBranchAssignmentRepository userBranchAssignmentRepository;
    private final BranchRepository branchRepository;
    private final PositionRepository positionRepository;
    private final ViewerAccessService viewerAccessService;

    public Set<Long> currentStaffBranchIds() {
        User principal = SecurityUtil.getCurrentUser();
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved");
        }

        User currentUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"));

        return staffBranchIds(currentUser);
    }

    /**
     * The branches this account may READ -- for country-level staff on a
     * read request that is every branch (see {@link #widenForCountryStaff}).
     */
    public Set<Long> staffBranchIds(User user) {
        return resolveScope(user, true);
    }

    /**
     * The branches this account actually STAFFS, never widened. Use it
     * wherever "can this person manage/edit it" is decided, including the
     * capability flags returned on a read, so country-level staff are not
     * offered edit actions on branches outside their own.
     */
    public Set<Long> staffOwnBranchIds(User user) {
        return resolveScope(user, false);
    }

    private Set<Long> resolveScope(User user, boolean widenForRead) {
        if (user == null || user.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role");
        }

        UserRole effectiveRole = viewerAccessService.baseReadRole(user);

        if (viewerAccessService.isViewer(user) &&
                (effectiveRole == UserRole.SECRETARY || effectiveRole == UserRole.BRANCH_LEADER)) {
            if (user.getBranchId() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Viewer branch scope is missing a branch");
            }
            Set<Long> viewerBranch = Set.of(user.getBranchId());

            // A viewer of the country-level branch reads every branch too
            // (they stay read-only: ViewerWriteBlockFilter blocks any write).
            return widenForRead ? widenForCountryStaff(viewerBranch) : viewerBranch;
        }

        if (effectiveRole == UserRole.SECRETARY) {
            Set<Long> ids = secretaryBranchIds(user);
            return widenForRead ? widenForCountryStaff(ids) : ids;
        }

        if (effectiveRole == UserRole.BRANCH_LEADER) {
            Long branchId = branchLeaderBranchId(user);
            return widenForRead
                    ? widenForCountryStaff(Set.of(branchId))
                    : Set.of(branchId);
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This role does not use staff branch scope");
    }

    /**
     * Staff of the single COUNTRY-level branch get two scopes from the one
     * method every list/detail/dashboard read already goes through: on a
     * read (GET/HEAD) they see every branch, like an admin; on anything
     * that changes data they only have their own country branch, like any
     * other secretary or leader -- so activities, donations and documents
     * can be viewed everywhere but only created in the country branch
     * itself. (Members and branches get their own explicit allowance, see
     * {@link #isCountryStaff}.)
     *
     * Reads outside a web request (scheduled jobs, notifications) have no
     * request method, so they keep the own-branch scope.
     */
    private Set<Long> widenForCountryStaff(Set<Long> ownBranchIds) {
        if (!isReadOnlyRequest()
                || ownBranchIds.size() != 1
                || !isCountryBranch(ownBranchIds.iterator().next())) {
            return ownBranchIds;
        }

        return Set.copyOf(branchRepository.findAllBranchIds());
    }

    private boolean isReadOnlyRequest() {
        if (!(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes)) {
            return false;
        }

        String method = attributes.getRequest().getMethod();

        return "GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method);
    }

    /**
     * The country branch's staff are single-branch accounts: never a
     * regional secretary (whose coverage is computed from the anchor
     * branch) -- their wider read access comes from
     * {@link #widenForCountryStaff}, not from either of those. Callers that
     * anchor a regional secretary reject the country branch with this.
     */
    public void forbidRegionalAtCountryBranch(Long anchorBranchId) {
        if (isCountryBranch(anchorBranchId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The country-level branch cannot have a regional secretary");
        }
    }

    /**
     * Same reason for the multi-branch secretary: the country branch can
     * only ever be someone's single branch, never one of several.
     */
    public void forbidCountryBranchAmongSeveral(
            java.util.Collection<Long> branchIds
    ) {
        if (branchIds == null || branchIds.size() < 2) {
            return;
        }

        for (Long branchId : branchIds) {
            if (isCountryBranch(branchId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The country-level branch cannot be combined with other branches for one secretary");
            }
        }
    }

    public boolean isCountryBranch(Long branchId) {
        if (branchId == null) {
            return false;
        }

        return branchRepository.findById(branchId)
                .flatMap(branch ->
                        branchRepository.findLevelCodeById(
                                branch.getBranchLevelId()
                        ))
                .map("COUNTRY"::equals)
                .orElse(false);
    }

    /**
     * Whether this account is a branch leader or secretary of the
     * COUNTRY-level branch -- the "semi-admin" accounts. Their reads are
     * widened by {@link #widenForCountryStaff}; they may also create and
     * edit branches and members anywhere (checked by the callers that
     * allow it), but never see the users or variable pages.
     */
    public boolean isCountryStaff(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }

        // A viewer scoped as secretary/branch leader of the country branch
        // counts too: they get the same wide reading, and stay read-only
        // because ViewerWriteBlockFilter blocks every write they could make.
        // Their branch is the one on the account (what their own scope
        // resolution uses), not the member's.
        if (viewerAccessService.isViewer(user)) {
            UserRole scope = viewerAccessService.baseReadRole(user);

            return (scope == UserRole.SECRETARY || scope == UserRole.BRANCH_LEADER)
                    && isCountryBranch(user.getBranchId());
        }

        if (user.getRole() != UserRole.SECRETARY
                && user.getRole() != UserRole.BRANCH_LEADER) {
            return false;
        }

        return isCountryBranch(homeBranchIdOf(user));
    }

    /**
     * Country-level staff on a read request -- the case where
     * {@link ViewerAccessService#effectiveReadRole} lets them read as an
     * admin. False on anything that changes data.
     */
    public boolean isCountryStaffReading(User user) {
        if (user == null
                || user.getId() == null
                || !isReadOnlyRequest()) {
            return false;
        }

        // effectiveReadRole is asked many times per request; the answer
        // cannot change within one, so remember it on the request instead
        // of re-reading the member and branch each time.
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String key = "tnal.countryStaff." + user.getId();
        Object cached = attributes.getAttribute(
                key,
                RequestAttributes.SCOPE_REQUEST
        );

        if (cached instanceof Boolean value) {
            return value;
        }

        boolean result = isCountryStaff(user);
        attributes.setAttribute(key, result, RequestAttributes.SCOPE_REQUEST);

        return result;
    }

    private Long homeBranchIdOf(User user) {
        if (user.getMemberId() != null) {
            return memberRepository.findById(user.getMemberId())
                    .map(Member::getBranchId)
                    .orElse(user.getBranchId());
        }

        return user.getBranchId();
    }

    /**
     * The country branch the signed-in account staffs, or empty when they
     * are not country-level staff -- the frontend uses this to offer the
     * "all branches" / "my branch" switch.
     */
    public java.util.Optional<Branch> currentUserCountryBranch() {
        User principal = SecurityUtil.getCurrentUser();

        if (principal == null || principal.getId() == null) {
            return java.util.Optional.empty();
        }

        User user = userRepository.findById(principal.getId()).orElse(null);

        if (!isCountryStaff(user)) {
            return java.util.Optional.empty();
        }

        return branchRepository.findById(homeBranchIdOf(user));
    }

    /**
     * For services that decide branch scope from the raw role string rather
     * than {@link ViewerAccessService#effectiveReadRole}: whether the
     * signed-in account is country-level staff on a read request, who read
     * organization-wide like an admin.
     */
    public boolean isCurrentUserCountryStaffReading() {
        if (!isReadOnlyRequest()) {
            return false;
        }

        User principal = SecurityUtil.getCurrentUser();

        if (principal == null || principal.getId() == null) {
            return false;
        }

        return userRepository.findById(principal.getId())
                .map(this::isCountryStaffReading)
                .orElse(false);
    }

    public boolean isCurrentUserCountryStaff() {
        User principal = SecurityUtil.getCurrentUser();

        if (principal == null || principal.getId() == null) {
            return false;
        }

        return userRepository.findById(principal.getId())
                .map(this::isCountryStaff)
                .orElse(false);
    }

    public void requireStaffBranchAccess(User user, Long branchId) {
        if (branchId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Branch ID is required");
        }
        if (!staffBranchIds(user).contains(branchId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have access to this branch");
        }
    }

    private Set<Long> secretaryBranchIds(User user) {
        LinkedHashSet<Long> branchIds = new LinkedHashSet<>();

        /*
         * Member-linked secretary:
         * members.branch_id is the home/primary branch and branch_staff
         * contains every active additional assignment. Do NOT also trust
         * users.branch_id here because it can become stale after a member
         * branch change and would accidentally keep access to an old branch.
         *
         * Standalone secretary:
         * user_branch_assignments holds the full covered-branch list (see
         * UserBranchAssignment's own docblock) -- fall back to the single
         * users.branch_id for an account created before that table
         * existed, or one that was never given any extra branches.
         */
        if (user.getMemberId() != null) {
            Member member =
                    memberRepository.findById(user.getMemberId()).orElse(null);

            Set<Long> regionalCoverage =
                    regionalSecretaryBranchIds(member);

            if (regionalCoverage != null) {
                return regionalCoverage;
            }

            branchIds.addAll(
                    branchStaffRepository.findActiveBranchIdsByMemberId(user.getMemberId()));

            if (member != null && member.getBranchId() != null) {
                branchIds.add(member.getBranchId());
            }
        } else {
            if (user.isRegionalSecretary()) {
                return computeRegionalCoverage(user.getBranchId());
            }

            branchIds.addAll(
                    userBranchAssignmentRepository.findBranchIdsByUserId(user.getId()));

            if (branchIds.isEmpty() && user.getBranchId() != null) {
                branchIds.add(user.getBranchId());
            }
        }

        if (branchIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Secretary is not assigned to any branch");
        }

        return Set.copyOf(branchIds);
    }

    /**
     * A "regional secretary"'s live coverage, or {@code null} if this
     * member isn't one -- the caller falls through to the normal
     * flat-list resolution in that case. Computed fresh from the
     * anchor branch (the member's own home branch) and the CURRENT
     * branch hierarchy on every call, by design -- see the
     * SECRETARY_REGIONAL mappedRole's own docblock on Position. A
     * province/district added under this anchor after the member was
     * assigned this position is included automatically, with no
     * re-save needed.
     */
    private Set<Long> regionalSecretaryBranchIds(Member member) {
        if (member == null
                || member.getId() == null
                || member.getBranchId() == null) {
            return null;
        }

        Short positionId =
                branchStaffRepository
                        .findActivePositionId(
                                member.getId(),
                                member.getBranchId()
                        )
                        .orElse(null);

        if (positionId == null) {
            return null;
        }

        Position position =
                positionRepository.findById(positionId).orElse(null);

        if (position == null
                || !"SECRETARY_REGIONAL".equals(position.getMappedRole())) {
            return null;
        }

        return computeRegionalCoverage(member.getBranchId());
    }

    /**
     * Whether this member currently holds a SECRETARY_REGIONAL position
     * (at their own branch). Same check real authorization uses, exposed
     * so write paths can restrict who may manage such a member.
     */
    public boolean isRegionalSecretary(Long memberId) {
        if (memberId == null) {
            return false;
        }

        return regionalSecretaryBranchIds(
                memberRepository.findById(memberId).orElse(null)
        ) != null;
    }

    /**
     * Every branch covered by a SECRETARY_REGIONAL position anchored at
     * the given branch -- public so the frontend's "preview the
     * computed branch list" call (see BranchController) can reuse the
     * exact same logic the live authorization check above uses,
     * instead of a second implementation that could drift out of sync.
     */
    public Set<Long> computeRegionalCoverage(Long anchorBranchId) {
        if (anchorBranchId == null) {
            return Set.of();
        }

        Branch anchor =
                branchRepository.findById(anchorBranchId).orElse(null);

        if (anchor == null) {
            return Set.of(anchorBranchId);
        }

        String levelCode =
                branchRepository
                        .findLevelCodeById(anchor.getBranchLevelId())
                        .orElse(null);

        if ("PROVINCE".equals(levelCode)) {
            return Set.copyOf(
                    branchRepository.findIdsByProvinceId(anchor.getProvinceId())
            );
        }

        if ("DISTRICT".equals(levelCode) && anchor.getDistrictId() != null) {
            return Set.copyOf(
                    branchRepository.findIdsByDistrictId(anchor.getDistrictId())
            );
        }

        // COMMUNE-anchored (or an unrecognized level) -- no expansion,
        // same as a plain single-branch secretary.
        return Set.of(anchor.getId());
    }

    private Long branchLeaderBranchId(User user) {
        Long branchId = user.getBranchId();

        if (branchId == null && user.getMemberId() != null) {
            branchId = memberRepository.findById(user.getMemberId())
                    .map(Member::getBranchId)
                    .orElse(null);
        }

        if (branchId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Branch leader account is not assigned to a branch");
        }

        return branchId;
    }
}
