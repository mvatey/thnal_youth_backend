package org.example.tnal_youth_backend.member.branch.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchOptionResponse;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchResponse;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.mapper.BranchMapper;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
             * Fallback when branch_staff has no active assignment.
             */
            if (accessibleBranchIds.isEmpty()) {
                Member member =
                        memberRepository
                                .findById(memberId)
                                .orElseThrow(() ->
                                        new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "Linked member record was not found"
                                        )
                                );

                if (member.getBranchId() != null) {
                    accessibleBranchIds.add(
                            member.getBranchId()
                    );
                }
            }

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
                request.districtId(),
                request.communeId()
        );

        String branchCode = normalizeRequired(
                request.branchCode(),
                "Branch code"
        );

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
                .createdById(request.createdById())
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
                request.districtId(),
                request.communeId()
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
            Integer districtId,
            Integer communeId
    ) {

        if (communeId != null && districtId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "District ID is required when commune ID is provided"
            );

        }

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
         * Admin can select any valid branch.
         */
        if (role == UserRole.ADMIN) {
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
}