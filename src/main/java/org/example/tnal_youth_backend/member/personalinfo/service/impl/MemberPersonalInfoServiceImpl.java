package org.example.tnal_youth_backend.member.personalinfo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.common.exception.ResourceNotFoundException;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.service.FileService;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.example.tnal_youth_backend.member.ethnicity.entity.Ethnicity;
import org.example.tnal_youth_backend.member.ethnicity.repository.EthnicityRepository;
import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.level.repository.MemberLevelRepository;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.member.nationality.entity.Nationality;
import org.example.tnal_youth_backend.member.nationality.repository.NationalityRepository;
import org.example.tnal_youth_backend.member.personalinfo.dto.request.UpdateMemberPersonalInfoRequest;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberAssignedBranchResponse;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberPersonalInfoService;
import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.religion.repository.ReligionRepository;
import org.example.tnal_youth_backend.myaccount.dto.request.UpdateMyPersonalInfoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPersonalInfoServiceImpl
        implements MemberPersonalInfoService {

    private static final long MAX_CV_SIZE =
            5L * 1024L * 1024L;

    private final MemberRepository memberRepository;

    private final ReligionRepository religionRepository;

    private final EthnicityRepository ethnicityRepository;

    private final NationalityRepository nationalityRepository;

    private final MemberLevelRepository memberLevelRepository;

    private final UserRepository userRepository;

    private final MemberAccessValidator memberAccessValidator;

    private final BranchService branchService;

    private final FileService fileService;

    private final BranchRepository branchRepository;

    private final BranchStaffRepository branchStaffRepository;

    /*
     * ==========================================================
     * GET PERSONAL INFORMATION
     * ==========================================================
     */

    @Override
    public MemberPersonalInfoResponse getPersonalInfo(
            Long memberId
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        Member member =
                findMember(
                        memberId
                );

        return toResponse(
                member
        );
    }

    /*
     * ==========================================================
     * UPDATE PERSONAL INFORMATION
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberPersonalInfoResponse updatePersonalInfo(
            Long memberId,
            UpdateMemberPersonalInfoRequest request
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Personal information request is required"
            );
        }

        Member member =
                findMember(
                        memberId
                );

        updateBasicInformation(member, request);
        updateReligion(member, request.religionId());
        updateEthnicity(member, request.ethnicityId());
        updateNationality(member, request.nationalityId());
        updateMemberLevel(member, request.memberLevelId());

        boolean branchChanged =
                member.getBranchId() == null
                        || !member.getBranchId()
                        .equals(
                                request.branchId()
                        );

        if (branchChanged) {
            memberAccessValidator
                    .validateCanManageSensitiveFields(
                            memberId
                    );

            updateBranch(
                    member,
                    request.branchId()
            );
        }

        Member savedMember =
                memberRepository
                        .saveAndFlush(
                                member
                        );

        synchronizeLinkedAccount(
                savedMember
        );

        return toResponse(
                savedMember
        );
    }

    /*
     * ==========================================================
     * UPDATE BASIC FIELDS
     * ==========================================================
     */

    private void updateBasicInformation(
            Member member,
            UpdateMemberPersonalInfoRequest request
    ) {
        member.setFullNameKm(
                normalizeRequiredText(
                        request.fullNameKm(),
                        "Khmer full name"
                )
        );

        member.setFullNameEn(
                normalizeText(
                        request.fullNameEn()
                )
        );

        member.setGender(
                request.gender()
        );

        member.setDateOfBirth(
                request.dateOfBirth()
        );

        member.setEmail(
                normalizeEmail(
                        request.email()
                )
        );

        member.setPhone(
                normalizeText(
                        request.phone()
                )
        );

        member.setCurrentAddress(
                normalizeText(
                        request.currentAddress()
                )
        );

        member.setPermanentAddress(
                normalizeText(
                        request.permanentAddress()
                )
        );

        member.setTshirtSize(
                request.tshirtSize()
        );
    }

    /*
     * ==========================================================
     * UPDATE RELIGION
     * ==========================================================
     */

    private void updateReligion(
            Member member,
            Short religionId
    ) {
        if (religionId == null) {
            member.setReligion(null);
            return;
        }

        Religion religion =
                religionRepository
                        .findById(
                                religionId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Religion not found with ID: "
                                                + religionId
                                )
                        );

        member.setReligion(
                religion
        );
    }

    /*
     * ==========================================================
     * UPDATE ETHNICITY
     * ==========================================================
     */

    private void updateEthnicity(
            Member member,
            Short ethnicityId
    ) {
        if (ethnicityId == null) {
            member.setEthnicity(null);
            return;
        }

        Ethnicity ethnicity =
                ethnicityRepository
                        .findById(
                                ethnicityId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Ethnicity not found with ID: "
                                                + ethnicityId
                                )
                        );

        member.setEthnicity(
                ethnicity
        );
    }

    /*
     * ==========================================================
     * UPDATE NATIONALITY
     * ==========================================================
     */

    private void updateNationality(
            Member member,
            Short nationalityId
    ) {
        if (nationalityId == null) {
            member.setNationality(null);
            return;
        }

        Nationality nationality =
                nationalityRepository
                        .findById(
                                nationalityId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Nationality not found with ID: "
                                                + nationalityId
                                )
                        );

        member.setNationality(
                nationality
        );
    }

    /*
     * ==========================================================
     * UPDATE MEMBER LEVEL
     * ==========================================================
     */

    private void updateMemberLevel(
            Member member,
            Short memberLevelId
    ) {
        if (memberLevelId == null) {
            member.setLevel(null);
            return;
        }

        MemberLevel memberLevel =
                memberLevelRepository
                        .findById(
                                memberLevelId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Member level not found with ID: "
                                                + memberLevelId
                                )
                        );

        member.setLevel(
                memberLevel
        );
    }

    /*
     * ==========================================================
     * UPDATE BRANCH
     * ==========================================================
     */

    private void updateBranch(
            Member targetMember,
            Long destinationBranchId
    ) {
        if (destinationBranchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch is required"
            );
        }

        if (destinationBranchId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID must be greater than zero"
            );
        }

        /*
         * No branch change.
         */
        if (
                targetMember.getBranchId() != null
                        && targetMember
                        .getBranchId()
                        .equals(destinationBranchId)
        ) {
            return;
        }

        UserRole actorRole =
                getCurrentActorRole();

        User targetUser =
                userRepository
                        .findByMemberId(
                                targetMember.getId()
                        )
                        .orElse(null);

        /*
         * A member without a user account is treated as
         * a normal MEMBER for branch-management purposes.
         */
        UserRole targetRole =
                targetUser == null
                        || targetUser.getRole() == null
                        ? UserRole.MEMBER
                        : targetUser.getRole();

        validateTargetCanBeMoved(
                actorRole,
                targetRole
        );

        /*
         * This method should validate:
         *
         * ADMIN:
         * - may select any existing branch.
         *
         * SECRETARY / BRANCH_LEADER:
         * - may select only branches in their accessible scope.
         */
        Branch destinationBranch =
                branchService
                        .getAccessibleBranchById(
                                destinationBranchId
                        );

        targetMember.setBranchId(
                destinationBranch.getId()
        );
    }

    /*
     * ==========================================================
     * VALIDATE WHO MAY MOVE THE TARGET
     * ==========================================================
     */

    private void validateTargetCanBeMoved(
            UserRole actorRole,
            UserRole targetRole
    ) {
        if (actorRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account does not have a valid role"
            );
        }

        if (targetRole == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN accounts cannot be moved from the member page"
            );
        }

        /*
         * ADMIN may move MEMBER, SECRETARY,
         * or BRANCH_LEADER to any branch.
         */
        if (actorRole == UserRole.ADMIN) {
            return;
        }

        /*
         * BRANCH_LEADER may move MEMBER or SECRETARY only.
         * Current-member scope is checked by
         * validateAccessibleMember(memberId).
         *
         * Destination scope is checked by
         * branchService.getAccessibleBranchById(branchId).
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
                    "Branch leader can only move MEMBER "
                            + "or SECRETARY accounts"
            );
        }

        /*
         * SECRETARY may move MEMBER accounts only.
         */
        if (actorRole == UserRole.SECRETARY) {
            if (targetRole == UserRole.MEMBER) {
                return;
            }

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Secretary can only move MEMBER accounts"
            );
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to change member branches"
        );
    }

    /*
     * ==========================================================
     * FIND MEMBER
     * ==========================================================
     */

    private Member findMember(
            Long memberId
    ) {
        if (
                memberId == null
                        || memberId <= 0
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID must be greater than zero"
            );
        }

        return memberRepository
                .findById(
                        memberId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Member not found with ID: "
                                        + memberId
                        )
                );
    }

    /*
     * ==========================================================
     * RESPONSE MAPPING
     * ==========================================================
     */

    private MemberPersonalInfoResponse toResponse(
            Member member
    ) {
        User user =
                userRepository
                        .findByMemberId(
                                member.getId()
                        )
                        .orElse(null);

        Branch primaryBranch =
                member.getBranchId() == null
                        ? null
                        : branchRepository
                        .findById(
                                member.getBranchId()
                        )
                        .orElse(null);

        List<MemberAssignedBranchResponse>
                assignedBranches =
                getAssignedBranches(
                        member.getId()
                );

        return new MemberPersonalInfoResponse(
                member.getId(),

                member.getFullNameKm(),

                member.getFullNameEn(),

                member.getGender(),

                member.getDateOfBirth(),

                member.getEmail(),

                member.getPhone(),

                member.getReligion() != null
                        ? member.getReligion().getId()
                        : null,

                member.getEthnicity() != null
                        ? member.getEthnicity().getId()
                        : null,

                member.getNationality() != null
                        ? member.getNationality().getId()
                        : null,

                member.getLevel() != null
                        ? member.getLevel().getId()
                        : null,

                member.getBranchId(),

                primaryBranch != null
                        ? primaryBranch.getNameKm()
                        : null,

                primaryBranch != null
                        ? primaryBranch.getNameEn()
                        : null,

                assignedBranches,

                member.getTshirtSize(),

                member.getCurrentAddress(),

                member.getPermanentAddress(),

                member.getCvFile() != null
                        ? member.getCvFile().getId()
                        : null,

                user != null
                        ? user.getId()
                        : null,

                user != null,

                user != null
                        ? user.getRole()
                        : null,

                user != null
                        ? user.getStatus()
                        : null
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
     * NORMALIZATION
     * ==========================================================
     */

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        String normalizedValue =
                normalizeText(
                        value
                );

        if (normalizedValue == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        return normalizedValue;
    }

    private String normalizeText(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(
            String email
    ) {
        String normalizedEmail =
                normalizeText(
                        email
                );

        return normalizedEmail == null
                ? null
                : normalizedEmail.toLowerCase(
                Locale.ROOT
        );
    }

    private String normalizeUppercaseText(
            String value
    ) {
        String normalizedValue =
                normalizeText(
                        value
                );

        return normalizedValue == null
                ? null
                : normalizedValue.toUpperCase(
                Locale.ROOT
        );
    }

    @Override
    @Transactional
    public MemberPersonalInfoResponse uploadCv(
            Long memberId,
            MultipartFile file
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        Member member =
                findMember(
                        memberId
                );

        validateCvFile(
                file
        );

        FileEntity uploadedFile =
                fileService.uploadFileEntity(
                        file
                );

        member.setCvFile(
                uploadedFile
        );

        Member savedMember =
                memberRepository.saveAndFlush(
                        member
                );

        return toResponse(
                savedMember
        );
    }

    private void validateCvFile(
            MultipartFile file
    ) {
        if (
                file == null
                        || file.isEmpty()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CV file is required"
            );
        }

        if (file.getSize() > MAX_CV_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CV file must not exceed 5 MB"
            );
        }

        String originalFilename =
                file.getOriginalFilename();

        if (
                originalFilename == null
                        || originalFilename.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CV file name is required"
            );
        }

        String extension =
                getFileExtension(
                        originalFilename
                );

        boolean allowed =
                extension.equals("pdf")
                        || extension.equals("docx")
                        || extension.equals("jpg")
                        || extension.equals("jpeg")
                        || extension.equals("png");

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF, DOCX, JPG, JPEG, and PNG files are allowed"
            );
        }
    }

    private String getFileExtension(
            String filename
    ) {
        int lastDotIndex =
                filename.lastIndexOf('.');

        if (
                lastDotIndex < 0
                        || lastDotIndex
                        == filename.length() - 1
        ) {
            return "";
        }

        return filename
                .substring(
                        lastDotIndex + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }

    @Override
    @Transactional
    public MemberPersonalInfoResponse updateMyPersonalInfo(
            Long memberId,
            UpdateMyPersonalInfoRequest request
    ) {
        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Personal information request is required"
            );
        }

        Member member =
                findMember(
                        memberId
                );

        member.setFullNameKm(
                normalizeRequiredText(
                        request.fullNameKm(),
                        "Khmer full name"
                )
        );

        member.setFullNameEn(
                normalizeText(
                        request.fullNameEn()
                )
        );

        member.setGender(
                request.gender()
        );

        member.setDateOfBirth(
                request.dateOfBirth()
        );

        member.setEmail(
                normalizeEmail(
                        request.email()
                )
        );

        member.setPhone(
                normalizeText(
                        request.phone()
                )
        );

        member.setCurrentAddress(
                normalizeText(
                        request.currentAddress()
                )
        );

        member.setPermanentAddress(
                normalizeText(
                        request.permanentAddress()
                )
        );

        member.setTshirtSize(
                request.tshirtSize()
        );

        updateReligion(
                member,
                request.religionId()
        );

        updateEthnicity(
                member,
                request.ethnicityId()
        );

        updateNationality(
                member,
                request.nationalityId()
        );

        updateMemberLevel(
                  member,
                  request.memberLevelId()
          );

          member.setJoinedOn(request.joinedOn());

        Member savedMember =
                memberRepository
                        .saveAndFlush(
                                member
                        );

        synchronizeLinkedAccount(
                savedMember
        );

        return toResponse(
                savedMember
        );
    }

    private void synchronizeLinkedAccount(
            Member member
    ) {
        userRepository
                .findByMemberId(
                        member.getId()
                )
                .ifPresent(user -> {
                    String newPhone =
                            member.getPhone();

                    String newEmail =
                            member.getEmail();

                    if (newPhone != null
                            && !newPhone.isBlank()
                            && userRepository.existsByPhoneAndIdNot(
                                    newPhone,
                                    user.getId()
                            )) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "This phone number already exists. Please use a different one."
                        );
                    }

                    if (newEmail != null
                            && !newEmail.isBlank()
                            && userRepository.existsByEmailIgnoreCaseAndIdNot(
                                    newEmail,
                                    user.getId()
                            )) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "This email already exists. Please use a different one."
                        );
                    }

                    user.setFullNameKm(
                            member.getFullNameKm()
                    );

                    user.setFullNameEn(
                            member.getFullNameEn()
                    );

                    user.setPhone(
                            newPhone
                    );

                    user.setEmail(
                            newEmail
                    );

                    /*
                     * Keep these unchanged:
                     *
                     * user.role
                     * user.status
                     * user.memberId
                     */
                    userRepository.saveAndFlush(
                            user
                    );
                });
    }

    private List<MemberAssignedBranchResponse>
    getAssignedBranches(
            Long memberId
    ) {
        Set<Long> branchIds =
                branchStaffRepository
                        .findActiveBranchIdsByMemberId(
                                memberId
                        );

        if (branchIds == null
                || branchIds.isEmpty()) {
            return List.of();
        }

        return branchRepository
                .findAllById(branchIds)
                .stream()
                .map(branch ->
                        new MemberAssignedBranchResponse(
                                branch.getId(),
                                branch.getNameKm(),
                                branch.getNameEn()
                        )
                )
                .toList();
    }
}
