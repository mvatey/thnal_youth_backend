package org.example.tnal_youth_backend.member.branch.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.Role;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.dto.projection.BranchManagementProjection;
import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.*;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.mapper.BranchMapper;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final BranchStaffRepository branchStaffRepository;
    private final ActivityRepository activityRepository;
    private final StaffBranchScopeService staffBranchScopeService;


    @Override
    @Transactional(readOnly = true)
    public List<BranchOptionResponse>
    getAccessibleBranchOptions() {

        User principal =
                SecurityUtil.getCurrentUser();

        if (principal == null
                || principal.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        User currentUser =
                userRepository
                        .findById(principal.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found"
                                )
                        );

        UserRole role =
                currentUser.getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        List<Branch> branches;

        if (role == UserRole.ADMIN) {
            branches =
                    branchRepository
                            .findAllActiveBranches();

        } else if (
                role == UserRole.SECRETARY
                        || role == UserRole.BRANCH_LEADER
        ) {
            Set<Long> accessibleBranchIds =
                    staffBranchScopeService
                            .staffBranchIds(currentUser);

            branches =
                    branchRepository
                            .findActiveByIds(
                                    accessibleBranchIds
                            );

        } else {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access branch options"
            );
        }

        return branches
                .stream()
                .map(branch ->
                        new BranchOptionResponse(
                                branch.getId(),
                                branch.getBranchCode(),
                                branch.getNameKm(),
                                branch.getNameEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchOptionResponse> getAllActiveBranchOptions() {
        return branchRepository
                .findAllActiveBranches()
                .stream()
                .map(branch ->
                        new BranchOptionResponse(
                                branch.getId(),
                                branch.getBranchCode(),
                                branch.getNameKm(),
                                branch.getNameEn()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(branchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long id) {
        return branchMapper.toResponse(
                findBranchById(id)
        );
    }

    @Override
    @Transactional
    public BranchResponse createBranch(
            CreateBranchRequest request
    ) {

        validateLocation(
                request.branchLevelId(),
                request.provinceId(),
                request.districtId(),
                request.communeId()
        );

        validateUniqueBranchLocation(
                request.branchLevelId(),
                request.provinceId(),
                request.districtId(),
                request.communeId(),
                null
        );

        String branchCode =
                generateNextBranchCode();

        String nameKm = normalizeRequired(
                request.nameKm(),
                "Khmer branch name"
        );

        validateDuplicate(
                nameKm,
                request.provinceId(),
                request.districtId(),
                request.communeId(),
                null
        );

        if (request.parentBranchId() != null) {
            findBranchById(request.parentBranchId());
        }

        Branch branch = Branch.builder()
                .branchCode(branchCode)
                .nameKm(nameKm)
                .nameEn(trimToNull(request.nameEn()))
                .branchLevelId(request.branchLevelId())
                .parentBranchId(request.parentBranchId())
                .provinceId(request.provinceId())
                .districtId(request.districtId())
                .communeId(request.communeId())
                .statusId(request.statusId())
                .address(trimToNull(request.address()))
                .googleMapUrl(trimToNull(request.googleMapUrl()))
                .phone(trimToNull(request.phone()))
                .email(normalizeEmail(request.email()))
                .createdById(requireCurrentUserId())
                .build();

        try {

            Branch savedBranch =
                    branchRepository.saveAndFlush(branch);

            return branchMapper.toResponse(savedBranch);

        } catch (DataIntegrityViolationException exception) {

            throw databaseConstraintException();

        }

    }

    @Override
    @Transactional
    public BranchResponse updateBranch(
            Long id,
            UpdateBranchRequest request
    ) {
        Branch branch = findBranchById(id);

        validateLocation(
                request.branchLevelId(),
                request.provinceId(),
                request.districtId(),
                request.communeId()
        );

        validateUniqueBranchLocation(
                request.branchLevelId(),
                request.provinceId(),
                request.districtId(),
                request.communeId(),
                id
        );

        if (request.parentBranchId() != null) {
            if (request.parentBranchId().equals(id)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A branch cannot be its own parent"
                );
            }

            findBranchById(request.parentBranchId());
        }

        String nameKm = normalizeRequired(
                request.nameKm(),
                "Khmer branch name"
        );

        validateDuplicate(
                nameKm,
                request.provinceId(),
                request.districtId(),
                request.communeId(),
                id
        );

        branch.setNameKm(nameKm);
        branch.setNameEn(trimToNull(request.nameEn()));
        branch.setBranchLevelId(request.branchLevelId());
        branch.setParentBranchId(request.parentBranchId());
        branch.setProvinceId(request.provinceId());
        branch.setDistrictId(request.districtId());
        branch.setCommuneId(request.communeId());
        branch.setStatusId(request.statusId());
        branch.setAddress(trimToNull(request.address()));
        branch.setGoogleMapUrl(trimToNull(request.googleMapUrl()));
        branch.setPhone(trimToNull(request.phone()));
        branch.setEmail(normalizeEmail(request.email()));

        try {
            Branch updatedBranch =
                    branchRepository.saveAndFlush(branch);

            return branchMapper.toResponse(updatedBranch);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException();
        }
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {

        Branch branch = findBranchById(id);

        if (branchRepository.existsByParentBranchId(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a branch that has child branches"
            );

        }

        try {

            branchRepository.delete(branch);
            branchRepository.flush();

        } catch (DataIntegrityViolationException exception) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    Cannot delete this branch because it is being used
                    by members, activities, donations, documents,
                    users, staff, or another database record.
                    """
            );

        }

    }

    private Branch findBranchById(Long id) {

        if (id == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );

        }

        return branchRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Branch not found with ID: " + id
                        )
                );

    }

    private void validateLocation(
            Short branchLevelId,
            Short provinceId,
            Integer districtId,
            Integer communeId
    ) {
        if (branchLevelId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch level is required"
            );
        }

        if (provinceId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Province is required"
            );
        }

        /*
         * Province-level branch.
         */
        if (branchLevelId == 1) {
            if (
                    districtId != null
                            || communeId != null
            ) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Province-level branch cannot have district or commune"
                );
            }

            return;
        }

        /*
         * District-level branch.
         */
        if (branchLevelId == 2) {
            if (districtId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "District is required for a district-level branch"
                );
            }

            if (communeId != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "District-level branch cannot have a commune"
                );
            }

            return;
        }

        /*
         * Commune-level branch.
         */
        if (branchLevelId == 3) {
            if (districtId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "District is required for a commune-level branch"
                );
            }

            if (communeId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Commune is required for a commune-level branch"
                );
            }

            return;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported branch level ID: "
                        + branchLevelId
        );
    }

    private void validateDuplicate(
            String nameKm,
            Short provinceId,
            Integer districtId,
            Integer communeId,
            Long currentId
    ) {

        boolean duplicate;

        if (currentId == null) {

            duplicate =
                    branchRepository.existsDuplicateBranch(
                            nameKm,
                            provinceId,
                            districtId,
                            communeId
                    );

        } else {

            duplicate =
                    branchRepository.existsDuplicateBranchExceptId(
                            nameKm,
                            provinceId,
                            districtId,
                            communeId,
                            currentId
                    );

        }

        if (duplicate) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    A branch with the same Khmer name and location
                    already exists.
                    """
            );

        }

    }

    private String normalizeRequired(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );

        }

        return value.trim();

    }

    private String normalizeEmail(
            String email
    ) {

        String value = trimToNull(email);

        return value == null
                ? null
                : value.toLowerCase(Locale.ROOT);

    }

    private String trimToNull(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;

    }

    private ResponseStatusException databaseConstraintException() {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Branch could not be saved.
                Check that branch_code,
                branch_level_id,
                parent_branch_id,
                province_id,
                district_id,
                commune_id,
                status_id,
                and created_by reference existing records.
                """
        );

    }

    @Override
    @Transactional(readOnly = true)
    public BranchLeaderResponse getLeader(Long branchId) {
        findBranchById(branchId);
        return branchStaffRepository.findActiveLeader(branchId).orElse(null);
    }

    @Override
    @Transactional
    public BranchLeaderResponse assignLeader(Long branchId, Long memberId) {
        findBranchById(branchId);
        if (!branchStaffRepository.isActiveMemberOfBranch(branchId, memberId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The branch leader must be an active member of this branch");
        }

        branchStaffRepository.findActiveLeaderBranchIdByMemberId(memberId)
                .filter(existingBranchId -> !existingBranchId.equals(branchId))
                .ifPresent(existingBranchId -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "This member is already the active branch leader of branch "
                                    + existingBranchId
                    );
                });

        // A promoted branch leader must stop carrying secretary-style
        // additional branch assignments. Leader scope is exactly one branch.
        branchStaffRepository.endOtherActiveAssignmentsForLeader(
                memberId,
                branchId
        );
        branchStaffRepository.assignLeader(branchId, memberId, requireCurrentUserId());
        return branchStaffRepository.findActiveLeader(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Branch leader assignment could not be loaded"));
    }

    @Override
    @Transactional
    public void removeLeader(Long branchId) {
        findBranchById(branchId);
        branchStaffRepository.removeLeader(branchId);
    }

    private Long requireCurrentUserId() {
        User currentUser = SecurityUtil.getCurrentUser();

        if (currentUser == null || currentUser.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return currentUser.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Branch getAccessibleBranchById(
            Long branchId
    ) {
        if (branchId == null || branchId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID must be greater than zero"
            );
        }

        Branch branch =
                branchRepository
                        .findById(branchId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Branch not found with ID: "
                                                + branchId
                                )
                        );

        User principal =
                SecurityUtil.getCurrentUser();

        if (
                principal == null
                        || principal.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        User currentUser =
                userRepository
                        .findById(principal.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found"
                                )
                        );

        UserRole role =
                currentUser.getRole();

        /*
         * Admin can select any valid branch. VIEWER has the same viewing
         * authority as ADMIN throughout the app (see UserRole's doc
         * comment), so it gets the same unrestricted read here -- this
         * method never mutates anything, it only resolves which branch
         * the caller may look at.
         */
        if (role == UserRole.ADMIN || role == UserRole.VIEWER) {
            return branch;
        }

        if (
                role != UserRole.SECRETARY
                        && role != UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to select a branch"
            );
        }

        Long currentMemberId =
                currentUser.getMemberId();

        if (currentMemberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is not linked to a member record"
            );
        }

        Set<Long> accessibleBranchIds =
                new LinkedHashSet<>(
                        branchStaffRepository
                                .findActiveBranchIdsByMemberId(
                                        currentMemberId
                                )
                );

        /*
         * Fallback to the staff member's current branch when
         * branch_staff does not contain an active assignment.
         */
        if (accessibleBranchIds.isEmpty()) {
            Member currentMember =
                    memberRepository
                            .findById(currentMemberId)
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.FORBIDDEN,
                                            "Linked member record was not found"
                                    )
                            );

            if (currentMember.getBranchId() != null) {
                accessibleBranchIds.add(
                        currentMember.getBranchId()
                );
            }
        }

        if (!accessibleBranchIds.contains(branchId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The selected branch is outside your accessible scope"
            );
        }

        return branch;
    }

    @Override
    @Transactional(readOnly = true)
    public BranchSummaryResponse getBranchSummary() {
        User principal =
                SecurityUtil.getCurrentUser();

        if (
                principal == null
                        || principal.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        User currentUser =
                userRepository
                        .findById(
                                principal.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found"
                                )
                        );

        UserRole role =
                currentUser.getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        /*
         * Replace this later with status lookup by code
         * if ACTIVE is not always ID 1.
         */
        Short activeStatusId = 1;

        if (role == UserRole.ADMIN) {
            long totalBranches =
                    branchRepository.count();

            long activeBranches =
                    branchRepository
                            .countByStatusId(
                                    activeStatusId
                            );

            long totalMembers =
                    memberRepository.count();

            return new BranchSummaryResponse(
                    totalBranches,
                    activeBranches,
                    totalMembers
            );
        }

        if (
                role != UserRole.SECRETARY
                        && role != UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view branch summary"
            );
        }

        Long memberId =
                currentUser.getMemberId();

        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is not linked to a member record"
            );
        }

        Set<Long> accessibleBranchIds =
                new LinkedHashSet<>(
                        branchStaffRepository
                                .findActiveBranchIdsByMemberId(
                                        memberId
                                )
                );

        /*
         * Fallback to the member's primary branch.
         */
        if (accessibleBranchIds.isEmpty()) {
            Member currentMember =
                    memberRepository
                            .findById(
                                    memberId
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.FORBIDDEN,
                                            "Linked member record was not found"
                                    )
                            );

            if (currentMember.getBranchId() != null) {
                accessibleBranchIds.add(
                        currentMember.getBranchId()
                );
            }
        }

        if (accessibleBranchIds.isEmpty()) {
            return new BranchSummaryResponse(
                    0,
                    0,
                    0
            );
        }

        long totalBranches =
                branchRepository
                        .countByIdIn(
                                accessibleBranchIds
                        );

        long activeBranches =
                branchRepository
                        .countByIdInAndStatusId(
                                accessibleBranchIds,
                                activeStatusId
                        );

        long totalMembers =
                memberRepository
                        .countByBranchIdIn(
                                accessibleBranchIds
                        );

        return new BranchSummaryResponse(
                totalBranches,
                activeBranches,
                totalMembers
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BranchPageResponse getBranchPage(
            int page,
            int size,
            String search,
            Short levelId,
            Short provinceId,
            Integer districtId,
            Short statusId
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }

        if (levelId != null && levelId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch level ID must be greater than zero"
            );
        }

        if (provinceId != null && provinceId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Province ID must be greater than zero"
            );
        }

        if (districtId != null && districtId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "District ID must be greater than zero"
            );
        }

        if (statusId != null && statusId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status ID must be greater than zero"
            );
        }

        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim();

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        User principal =
                SecurityUtil.getCurrentUser();

        if (
                principal == null
                        || principal.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        User currentUser =
                userRepository
                        .findById(
                                principal.getId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found"
                                )
                        );

        UserRole role =
                currentUser.getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        Page<Branch> branchPage;

        /*
         * ADMIN can view all branches.
         */
        if (role == UserRole.ADMIN) {
            branchPage =
                    branchRepository
                            .findBranchPageForAdmin(
                                    normalizedSearch,
                                    levelId,
                                    provinceId,
                                    districtId,
                                    statusId,
                                    pageable
                            );

        } else if (
                role == UserRole.SECRETARY
                        || role == UserRole.BRANCH_LEADER
        ) {
            Long memberId =
                    currentUser.getMemberId();

            if (memberId == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Your account is not linked to a member record"
                );
            }

            Set<Long> accessibleBranchIds =
                    new LinkedHashSet<>(
                            branchStaffRepository
                                    .findActiveBranchIdsByMemberId(
                                            memberId
                                    )
                    );

            /*
             * Fallback to the member's primary branch.
             */
            if (accessibleBranchIds.isEmpty()) {
                Member currentMember =
                        memberRepository
                                .findById(
                                        memberId
                                )
                                .orElseThrow(() ->
                                        new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "Linked member record was not found"
                                        )
                                );

                if (currentMember.getBranchId() != null) {
                    accessibleBranchIds.add(
                            currentMember.getBranchId()
                    );
                }
            }

            /*
             * Avoid calling an IN query with an empty collection.
             */
            if (accessibleBranchIds.isEmpty()) {
                return new BranchPageResponse(
                        List.of(),
                        page,
                        size,
                        0,
                        0,
                        true,
                        true
                );
            }

            branchPage =
                    branchRepository
                            .findBranchPageByScope(
                                    accessibleBranchIds,
                                    normalizedSearch,
                                    levelId,
                                    provinceId,
                                    districtId,
                                    statusId,
                                    pageable
                            );

        } else {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view the branch page"
            );
        }

        List<BranchTableItemResponse> content =
                branchPage
                        .getContent()
                        .stream()
                        .map(this::toBranchTableItem)
                        .toList();

        return new BranchPageResponse(
                content,
                branchPage.getNumber(),
                branchPage.getSize(),
                branchPage.getTotalElements(),
                branchPage.getTotalPages(),
                branchPage.isFirst(),
                branchPage.isLast()
        );
    }

    private BranchTableItemResponse toBranchTableItem(
            Branch branch
    ) {
        long memberCount =
                memberRepository
                        .countByBranchId(
                                branch.getId()
                        );

        return new BranchTableItemResponse(
                branch.getId(),

                branch.getBranchCode(),

                branch.getNameKm(),

                branch.getNameEn(),

                branch.getBranchLevelId(),

                null, // branchLevelNameKm

                branch.getProvinceId(),

                null, // provinceNameKm

                branch.getDistrictId(),

                null, // districtNameKm

                branch.getCommuneId(),

                null, // communeNameKm

                memberCount,

                branch.getStatusId(),

                null, // statusNameKm

                branch.getCreatedAt()
        );
    }

    private Long getCurrentUserId() {
        User principal =
                SecurityUtil.getCurrentUser();

        if (
                principal == null
                        || principal.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return principal.getId();
    }

    private void validateUniqueBranchLocation(
            Short branchLevelId,
            Short provinceId,
            Integer districtId,
            Integer communeId,
            Long currentBranchId
    ) {
        boolean duplicate;

        /*
         * Province-level branch.
         */
        if (branchLevelId == 1) {
            duplicate =
                    currentBranchId == null
                            ? branchRepository
                            .existsByBranchLevelIdAndProvinceId(
                                    branchLevelId,
                                    provinceId
                            )
                            : branchRepository
                            .existsByBranchLevelIdAndProvinceIdAndIdNot(
                                    branchLevelId,
                                    provinceId,
                                    currentBranchId
                            );

            if (duplicate) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A province-level branch already exists for this province"
                );
            }

            return;
        }

        /*
         * District-level branch.
         */
        if (branchLevelId == 2) {
            duplicate =
                    currentBranchId == null
                            ? branchRepository
                            .existsByBranchLevelIdAndDistrictId(
                                    branchLevelId,
                                    districtId
                            )
                            : branchRepository
                            .existsByBranchLevelIdAndDistrictIdAndIdNot(
                                    branchLevelId,
                                    districtId,
                                    currentBranchId
                            );

            if (duplicate) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A district-level branch already exists for this district"
                );
            }

            return;
        }

        /*
         * Commune-level branch.
         */
        if (branchLevelId == 3) {
            duplicate =
                    currentBranchId == null
                            ? branchRepository
                            .existsByBranchLevelIdAndCommuneId(
                                    branchLevelId,
                                    communeId
                            )
                            : branchRepository
                            .existsByBranchLevelIdAndCommuneIdAndIdNot(
                                    branchLevelId,
                                    communeId,
                                    currentBranchId
                            );

            if (duplicate) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A commune-level branch already exists for this commune"
                );
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BranchDetailPageResponse getBranchDetails(
            Long branchId
    ) {
        Branch branch =
                getAccessibleBranchById(branchId);

        BranchDetailResponse branchResponse =
                branchMapper.toDetailResponse(branch);

        long totalMembers =
                memberRepository.countByBranchId(branchId);

        long totalActivities =
                activityRepository.countByBranchId(branchId);

        List<BranchLeaderResponse> leaders =
                memberRepository
                        .findBranchManagementMembers(
                                branchId,
                                List.of(
                                        UserRole.BRANCH_LEADER,
                                        UserRole.SECRETARY
                                )
                        )
                        .stream()
                        .map(item ->
                                branchMapper
                                        .toBranchLeaderResponse(
                                                item.getMember(),
                                                item.getRole()
                                        )
                        )
                        .toList();

        return new BranchDetailPageResponse(
                branchResponse,
                new BranchDetailSummaryResponse(
                        totalMembers,
                        totalActivities
                ),
                leaders
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BranchMemberPageResponse getBranchMembers(
            Long branchId,
            int page,
            int size,
            String search,
            Gender gender,
            Short statusId
    ) {
        /*
         * Validate that the current user may access
         * the requested branch.
         */
        getAccessibleBranchById(
                branchId
        );

        /*
         * Basic pagination validation.
         */
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }

        if (
                statusId != null
                        && statusId <= 0
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status ID must be greater than zero"
            );
        }

        /*
         * Normalize search so JPQL never receives null.
         */
        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim();

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        ).and(
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "id"
                                )
                        )
                );

        /*
         * The repository now returns both:
         *
         * - Member
         * - UserRole
         *
         * through BranchManagementProjection.
         */
        Page<BranchManagementProjection> result =
                memberRepository
                        .findBranchMembersExcludingRole(
                                branchId,
                                UserRole.BRANCH_LEADER,
                                normalizedSearch,
                                gender,
                                statusId,
                                pageable
                        );

        List<BranchMemberTableItemResponse> content =
                result
                        .getContent()
                        .stream()
                        .map(item ->
                                branchMapper
                                        .toBranchMemberTableItemResponse(
                                                item.getMember(),
                                                item.getRole()
                                        )
                        )
                        .toList();

        return new BranchMemberPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Override
    @Transactional
    public void assignBranchLeader(
            Long branchId,
            Long memberId
    ) {
        /*
         * Keep a single authoritative assignment path. This writes branch_staff,
         * enforces one active branch per BRANCH_LEADER, and synchronizes users.role.
         */
        assignLeader(branchId, memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchLeaderCandidateResponse>
    getBranchLeaderCandidates(
            Long branchId
    ) {
        /*
         * Also validates that the authenticated user
         * may access this branch.
         */
        getAccessibleBranchById(branchId);

        return memberRepository
                .findBranchLeaderCandidates(
                        branchId,
                        List.of(
                                UserRole.MEMBER,
                                UserRole.SECRETARY
                        )
                )
                .stream()
                .map(candidate ->
                        branchMapper
                                .toBranchLeaderCandidateResponse(
                                        candidate.getMember(),
                                        candidate.getRole()
                                )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getAccessibleBranchIds() {

        User principal =
                SecurityUtil.getCurrentUser();

        if (
                principal == null
                        || principal.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        User currentUser =
                userRepository
                        .findById(principal.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated user was not found"
                                )
                        );

        UserRole role =
                currentUser.getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        /*
         * ADMIN:
         * access to every branch.
         *
         * VIEWER gets the same unrestricted read scope as ADMIN here (see
         * UserRole's doc comment) -- this method only ever backs read
         * paths (e.g. DocumentServiceImpl.getDocuments), so widening it
         * to VIEWER cannot expose any mutating capability.
         */
        if (role == UserRole.ADMIN || role == UserRole.VIEWER) {
            return branchRepository
                    .findAll()
                    .stream()
                    .map(Branch::getId)
                    .collect(
                            java.util.stream.Collectors
                                    .toCollection(
                                            LinkedHashSet::new
                                    )
                    );
        }

        /*
         * Only Secretary and Branch Leader
         * use staff branch scope.
         */
        if (
                role != UserRole.SECRETARY
                        && role != UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access branch scope"
            );
        }

        return staffBranchScopeService
                .staffBranchIds(currentUser);
    }

    private String generateNextBranchCode() {
        long nextNumber =
                branchRepository.count() + 1;

        String branchCode =
                String.format(
                        "BR-%04d",
                        nextNumber
                );

        while (
                branchRepository.existsByBranchCode(
                        branchCode
                )
        ) {
            nextNumber++;

            branchCode =
                    String.format(
                            "BR-%04d",
                            nextNumber
                    );
        }

        return branchCode;
    }
}
