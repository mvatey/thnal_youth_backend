package org.example.tnal_youth_backend.member.branch.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.member.branch.dto.request.CreateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.request.UpdateBranchRequest;
import org.example.tnal_youth_backend.member.branch.dto.response.BranchResponse;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.mapper.BranchMapper;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

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
                .nameKm(nameKm)
                .nameEn(trimToNull(request.nameEn()))
                .branchLevelId(request.branchLevelId())
                .parentBranchId(request.parentBranchId())
                .provinceId(request.provinceId())
                .districtId(request.districtId())
                .communeId(request.communeId())
                .statusId(request.statusId())
                .address(trimToNull(request.address()))
                .googleMapUrl(
                        trimToNull(request.googleMapUrl())
                )
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
        branch.setNameEn(
                trimToNull(request.nameEn())
        );
        branch.setBranchLevelId(
                request.branchLevelId()
        );
        branch.setParentBranchId(
                request.parentBranchId()
        );
        branch.setProvinceId(
                request.provinceId()
        );
        branch.setDistrictId(
                request.districtId()
        );
        branch.setCommuneId(
                request.communeId()
        );
        branch.setStatusId(
                request.statusId()
        );
        branch.setAddress(
                trimToNull(request.address())
        );
        branch.setGoogleMapUrl(
                trimToNull(request.googleMapUrl())
        );
        branch.setPhone(
                trimToNull(request.phone())
        );
        branch.setEmail(
                normalizeEmail(request.email())
        );

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
                    Cannot delete this branch because it is being used \
                    by members, activities, donations, documents, users, \
                    staff, or another database record.
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
                    branchRepository
                            .existsDuplicateBranchExceptId(
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
                    A branch with the same Khmer name and location \
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

    private String normalizeEmail(String email) {
        String value = trimToNull(email);

        return value == null
                ? null
                : value.toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private ResponseStatusException
    databaseConstraintException() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Branch could not be saved. Check that branch_level_id, \
                parent_branch_id, province_id, district_id, commune_id, \
                status_id, and created_by reference existing records.
                """
        );
    }
}