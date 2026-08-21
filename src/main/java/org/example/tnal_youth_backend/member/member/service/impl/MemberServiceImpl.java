package org.example.tnal_youth_backend.member.member.service.impl;

import lombok.RequiredArgsConstructor;

import org.example.tnal_youth_backend.activity.repository.ActivityParticipantRepository;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;

import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;

import org.example.tnal_youth_backend.common.exception.ResourceNotFoundException;

import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.file.service.FileService;

import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;

import org.example.tnal_youth_backend.member.ethnicity.entity.Ethnicity;
import org.example.tnal_youth_backend.member.ethnicity.repository.EthnicityRepository;

import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.level.repository.MemberLevelRepository;

import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberProfilePhotoRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberStatusRequest;

import org.example.tnal_youth_backend.member.member.dto.response.*;

import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.entity.TshirtSize;

import org.example.tnal_youth_backend.member.member.mapper.MemberMapper;
import org.example.tnal_youth_backend.member.member.repository.MemberDetailSummaryRepository;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.member.member.service.MemberService;

import org.example.tnal_youth_backend.member.nationality.entity.Nationality;
import org.example.tnal_youth_backend.member.nationality.service.NationalityService;

import org.example.tnal_youth_backend.member.position.entity.Position;
import org.example.tnal_youth_backend.member.position.repository.PositionRepository;

import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.religion.repository.ReligionRepository;

import org.example.tnal_youth_backend.member.status.entity.MemberStatus;
import org.example.tnal_youth_backend.member.status.repository.MemberStatusRepository;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private static final String BUDDHISM_CODE =
            "BUDDHISM";

    private static final String ISLAM_CODE =
            "ISLAM";

    private static final Set<String>
            ALLOWED_TSHIRT_SIZES =
            Set.of(
                    "XS",
                    "S",
                    "M",
                    "L",
                    "XL",
                    "2XL",
                    "3XL"
            );

    private final MemberRepository
            memberRepository;

    private final UserRepository
            userRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final FileRepository
            fileRepository;

    private final FileService
            fileService;

    private final MemberStatusRepository
            memberStatusRepository;

    private final MemberLevelRepository
            memberLevelRepository;

    private final ReligionRepository
            religionRepository;

    private final EthnicityRepository
            ethnicityRepository;

    private final NationalityService
            nationalityService;

    private final MemberMapper
            memberMapper;

    private final BranchRepository
            branchRepository;

    private final BranchStaffRepository
            branchStaffRepository;

    private final PositionRepository
            positionRepository;

    private final ActivityParticipantRepository
            activityParticipantRepository;

    private final MemberDetailSummaryRepository
            memberDetailSummaryRepository;

    private final ActivityRepository
            activityRepository;

    private final MemberAccessValidator
            memberAccessValidator;


    /*
     * ==========================================================
     * GET MEMBER SUMMARY
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public MemberSummaryResponse getMemberSummary(Long requestedBranchId) {

        Long effectiveBranchId =
                resolveMemberListBranchId(
                        requestedBranchId
                );

        long totalMembers =
                effectiveBranchId == null
                        ? memberRepository
                        .count()
                        : memberRepository
                        .countByBranchId(
                                effectiveBranchId
                        );

        long femaleMembers =
                effectiveBranchId == null
                        ? memberRepository
                        .countByGender(
                                Gender.FEMALE
                        )
                        : memberRepository
                        .countByGenderAndBranchId(
                                Gender.FEMALE,
                                effectiveBranchId
                        );

        long monkMembers =
                effectiveBranchId == null
                        ? memberRepository
                        .countByGender(
                                Gender.MONK
                        )
                        : memberRepository
                        .countByGenderAndBranchId(
                                Gender.MONK,
                                effectiveBranchId
                        );

        long buddhistMembers =
                effectiveBranchId == null
                        ? memberRepository
                        .countByReligionCode(
                                BUDDHISM_CODE
                        )
                        : memberRepository
                        .countByReligionCodeAndBranchId(
                                BUDDHISM_CODE,
                                effectiveBranchId
                        );

        long islamMembers =
                effectiveBranchId == null
                        ? memberRepository
                        .countByReligionCode(
                                ISLAM_CODE
                        )
                        : memberRepository
                        .countByReligionCodeAndBranchId(
                                ISLAM_CODE,
                                effectiveBranchId
                        );

        return new MemberSummaryResponse(
                totalMembers,
                femaleMembers,
                monkMembers,
                buddhistMembers,
                islamMembers
        );
    }


    /*
     * ==========================================================
     * GET MEMBER LIST
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public MemberPageResponse getMembers(
            int page,
            int size,
            String search,
            Long branchId,
            Short statusId,
            String accountStatus,
            Gender gender
    ) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must not be negative"
            );
        }

        if (
                size < 1
                        || size > 100
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }

        if (
                branchId != null
                        && branchId <= 0
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID must be greater than zero"
            );
        }

        if (statusId != null) {
            findStatus(
                    statusId
            );
        }

        String normalizedAccountStatus = trimToNull(accountStatus);
        if (normalizedAccountStatus != null) {
            normalizedAccountStatus = normalizedAccountStatus.toUpperCase();
            try {
                org.example.tnal_youth_backend.authentication.model.enums.UserStatus
                        .valueOf(normalizedAccountStatus);
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid account status"
                );
            }
        }

        String normalizedSearch =
                trimToNull(
                        search
                );

        Long effectiveBranchId =
                resolveMemberListBranchId(
                        branchId
                );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        Set<Long> accessibleBranchIds =
                getAccessibleBranchIds();

        boolean unrestrictedScope =
                accessibleBranchIds
                        .isEmpty();

        Set<Long> queryBranchScope =
                unrestrictedScope
                        ? Set.of(-1L)
                        : accessibleBranchIds;

        if (
                branchId != null
                        && !unrestrictedScope
                        && !accessibleBranchIds
                        .contains(
                                branchId
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this branch"
            );
        }

        Page<Object[]> memberPage =
                memberRepository
                        .findMemberPage(
                                normalizedSearch,
                                branchId,
                                queryBranchScope,
                                unrestrictedScope,
                                statusId,
                                normalizedAccountStatus,
                                gender == null
                                        ? null
                                        : gender.name(),
                                pageable
                        );

        List<MemberListResponse> content =
                memberPage
                        .getContent()
                        .stream()
                        .map(
                                memberMapper
                                        ::toListResponse
                        )
                        .toList();

        return MemberPageResponse
                .builder()
                .content(
                        content
                )
                .page(
                        memberPage
                                .getNumber()
                )
                .size(
                        memberPage
                                .getSize()
                )
                .totalElements(
                        memberPage
                                .getTotalElements()
                )
                .totalPages(
                        memberPage
                                .getTotalPages()
                )
                .first(
                        memberPage
                                .isFirst()
                )
                .last(
                        memberPage
                                .isLast()
                )
                .build();
    }


    /*
     * ==========================================================
     * GET MEMBER BY ID
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberById(
            Long id
    ) {

        memberAccessValidator
                .validateAccessibleMember(
                        id
                );

        Member member =
                findDetailedMember(
                        id
                );

        return toMemberDetailResponse(
                member
        );
    }


    /*
     * ==========================================================
     * UPDATE PROFILE PHOTO
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberDetailResponse updateProfilePhoto(
            Long id,
            MultipartFile file
    ) {

        Member member =
                findDetailedMember(
                        id
                );

        validateMemberBranchAccess(
                member.getBranchId()
        );

        FileEntity uploadedPhoto =
                fileService
                        .uploadImage(
                                file,
                                getCurrentUserId()
                        );

        member.setProfilePhoto(
                uploadedPhoto
        );

        Member savedMember =
                memberRepository
                        .save(
                                member
                        );

        userRepository
                .findByMemberId(
                        id
                )
                .ifPresent(
                        user -> {
                            user.setProfileImage(
                                    uploadedPhoto
                                            .getFilePath()
                            );

                            userRepository
                                    .save(
                                            user
                                    );
                        }
                );

        return toMemberDetailResponse(
                savedMember
        );
    }


    /*
     * ==========================================================
     * CREATE MEMBER
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberDetailResponse createMember(
            CreateMemberRequest request
    ) {

        Branch branch =
                findBranch(
                        request
                                .branchId()
                );

        validateMemberBranchAccess(
                branch.getId()
        );

        String memberNo =
                generateMemberNo();

        String phone =
                trimToNull(
                        request.phone()
                );

        String email =
                normalizeEmail(
                        request.email()
                );

        validateUniqueValues(
                memberNo,
                phone,
                email,
                null
        );

        MemberStatus status =
                findStatus(
                        request.statusId()
                );

        MemberLevel level =
                findLevel(
                        request.levelId()
                );

        Nationality nationality =
                resolveNationality(
                        request.nationalityId()
                );

        User currentUser =
                getCurrentUser();

        Position position =
                request.positionId() == null
                        ? null
                        : positionRepository
                                .findById(
                                        request.positionId()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Position not found"
                                        )
                                );

        UserRole requestedRole =
                resolveRequestedRole(
                        position,
                        request.role()
                );

        validateAssignableRole(
                currentUser.getRole(),
                requestedRole
        );

        Member member =
                Member.builder()

                        .branchId(
                                branch.getId()
                        )

                        .memberNo(
                                memberNo
                        )

                        .fullNameKm(
                                normalizeRequired(
                                        request.fullNameKm(),
                                        "Khmer full name"
                                )
                        )

                        .fullNameEn(
                                trimToNull(
                                        request.fullNameEn()
                                )
                        )

                        .status(
                                status
                        )

                        .level(
                                level
                        )

                        .nationality(
                                nationality
                        )

                        .gender(
                                request.gender()
                        )

                        .dateOfBirth(
                                request.dateOfBirth()
                        )

                        .phone(
                                phone
                        )

                        .email(
                                email
                        )

                        .joinedOn(
                                request.joinedOn()
                        )

                        .profilePhoto(
                                findFile(
                                        request
                                                .profilePhotoId(),
                                        "Profile photo"
                                )
                        )

                        .createdById(
                                currentUser.getId()
                        )

                        .build();

        try {

            Member savedMember =
                    memberRepository
                            .saveAndFlush(
                                    member
                            );

            createPendingUserAccount(
                    savedMember,
                    requestedRole
            );

            if (position != null) {

                branchStaffRepository
                        .assignPosition(
                                savedMember
                                        .getBranchId(),
                                savedMember
                                        .getId(),
                                position
                                        .getId(),
                                request
                                        .joinedOn(),
                                currentUser
                                        .getId()
                        );
            }

            Member detailedMember =
                    findDetailedMember(
                            savedMember
                                    .getId()
                    );

            return toMemberDetailResponse(
                    detailedMember
            );

        } catch (
                DataIntegrityViolationException exception
        ) {

            throw createDatabaseException(
                    "Member could not be created",
                    exception
            );
        }
    }


    /*
     * ==========================================================
     * UPDATE MEMBER
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberDetailResponse updateMember(
            Long id,
            UpdateMemberRequest request
    ) {

        Member member =
                findDetailedMember(
                        id
                );

        validateMemberBranchAccess(
                member.getBranchId()
        );

        Branch targetBranch =
                findBranch(
                        request.branchId()
                );

        validateMemberBranchAccess(
                targetBranch.getId()
        );

        String phone =
                trimToNull(
                        request.phone()
                );

        String email =
                normalizeEmail(
                        request.email()
                );

        validateUniqueValues(
                member.getMemberNo(),
                phone,
                email,
                id
        );

        member.setFullNameKm(
                normalizeRequired(
                        request.fullNameKm(),
                        "Khmer full name"
                )
        );

        member.setFullNameEn(
                trimToNull(
                        request.fullNameEn()
                )
        );

        member.setBranchId(
                targetBranch.getId()
        );

        member.setStatus(
                findStatus(
                        request.statusId()
                )
        );

        member.setLevel(
                findLevel(
                        request.levelId()
                )
        );

        member.setReligion(
                findReligion(
                        request.religionId()
                )
        );

        member.setNationality(
                resolveNationality(
                        request.nationalityId()
                )
        );

        member.setEthnicity(
                findEthnicity(
                        request.ethnicityId()
                )
        );

        member.setGender(
                request.gender()
        );

        member.setDateOfBirth(
                request.dateOfBirth()
        );

        member.setPlaceOfBirth(
                trimToNull(
                        request.placeOfBirth()
                )
        );

        member.setTshirtSize(
                TshirtSize.fromValue(
                        request.tshirtSize()
                )
        );

        member.setPhone(
                phone
        );

        member.setEmail(
                email
        );

        member.setCurrentAddress(
                trimToNull(
                        request.currentAddress()
                )
        );

        member.setPermanentAddress(
                trimToNull(
                        request.permanentAddress()
                )
        );

        member.setProfilePhoto(
                findFile(
                        request.profilePhotoId(),
                        "Profile photo"
                )
        );

        member.setCvFile(
                findFile(
                        request.cvFileId(),
                        "CV file"
                )
        );

        member.setJoinedOn(
                request.joinedOn()
        );

        member.setBio(
                trimToNull(
                        request.bio()
                )
        );

        try {

            Member savedMember =
                    memberRepository
                            .saveAndFlush(
                                    member
                            );

            synchronizeLinkedUserAccount(
                    savedMember
            );

            Member detailedMember =
                    findDetailedMember(
                            savedMember
                                    .getId()
                    );

            return toMemberDetailResponse(
                    detailedMember
            );

        } catch (
                DataIntegrityViolationException exception
        ) {

            throw createDatabaseException(
                    "Member could not be updated",
                    exception
            );
        }
    }


    /*
     * ==========================================================
     * UPDATE MEMBER STATUS
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberDetailResponse updateMemberStatus(
            Long id,
            UpdateMemberStatusRequest request
    ) {

        Member member =
                findDetailedMember(
                        id
                );

        validateMemberBranchAccess(
                member.getBranchId()
        );

        MemberStatus status =
                findStatus(
                        request.statusId()
                );

        member.setStatus(
                status
        );

        try {

            memberRepository
                    .saveAndFlush(
                            member
                    );

            Member detailedMember =
                    findDetailedMember(
                            id
                    );

            return toMemberDetailResponse(
                    detailedMember
            );

        } catch (
                DataIntegrityViolationException exception
        ) {

            throw createDatabaseException(
                    "Member status could not be updated",
                    exception
            );
        }
    }


    /*
     * ==========================================================
     * DELETE MEMBER
     * ==========================================================
     */

    @Override
    @Transactional
    public void deleteMember(
            Long id
    ) {

        Member member =
                findDetailedMember(
                        id
                );

        validateMemberBranchAccess(
                member.getBranchId()
        );

        try {

            memberRepository
                    .delete(
                            member
                    );

            memberRepository
                    .flush();

        } catch (
                DataIntegrityViolationException exception
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,

                    getDatabaseErrorMessage(
                            """
                            This member cannot be deleted because related \
                            activity, donation, document, account, or other \
                            historical records already exist.
                            """,
                            exception
                    ),

                    exception
            );
        }
    }


    /*
     * ==========================================================
     * MEMBER DETAIL SUMMARY
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public MemberDetailSummaryResponse
    getMemberDetailSummary(
            Long memberId
    ) {

        Member member =
                findDetailedMember(
                        memberId
                );

        validateMemberBranchAccess(
                member.getBranchId()
        );

        long joinedActivityCount =
                activityParticipantRepository
                        .countParticipatedActivitiesByMemberId(
                                memberId
                        );

        long absentActivityCount =
                activityParticipantRepository
                        .countAbsentActivitiesByMemberId(
                                memberId
                        );

        long unregisteredCompletedActivityCount =
                activityRepository
                        .countCompletedRelevantActivitiesNotJoined(
                                memberId,
                                OffsetDateTime.now()
                        );

        long notJoinedActivityCount =
                absentActivityCount
                        + unregisteredCompletedActivityCount;

        MemberMonthlyDonationTotalResponse donationTotal =
                memberDetailSummaryRepository
                        .summarizeMonthlyDonationByMemberId(
                                memberId
                        );

        BigDecimal totalDonationKhr =
                donationTotal == null
                        || donationTotal
                        .getTotalKhr() == null
                        ? BigDecimal.ZERO
                        : donationTotal
                        .getTotalKhr();

        BigDecimal totalDonationUsd =
                donationTotal == null
                        || donationTotal
                        .getTotalUsd() == null
                        ? BigDecimal.ZERO
                        : donationTotal
                        .getTotalUsd();

        return new MemberDetailSummaryResponse(
                joinedActivityCount,
                notJoinedActivityCount,
                totalDonationKhr,
                totalDonationUsd
        );
    }


    @Override
    @Transactional(readOnly = true)
    public MemberMonthlyDonationSummaryResponse
    getMemberMonthlyDonationSummary(
            Long memberId
    ) {

        Member member =
                findDetailedMember(
                        memberId
                );

        validateMemberBranchAccess(
                member.getBranchId()
        );

        MemberMonthlyDonationSummaryResponse summary =
                memberDetailSummaryRepository
                        .summarizeMemberMonthlyDonations(
                                memberId
                        );

        if (summary == null) {

            return MemberMonthlyDonationSummaryResponse
                    .builder()
                    .donationCount(
                            0
                    )
                    .totalDonationKhr(
                            BigDecimal.ZERO
                    )
                    .totalDonationUsd(
                            BigDecimal.ZERO
                    )
                    .cashPaymentCount(
                            0
                    )
                    .bankPaymentCount(
                            0
                    )
                    .build();
        }

        if (
                summary
                        .getTotalDonationKhr() == null
        ) {
            summary.setTotalDonationKhr(
                    BigDecimal.ZERO
            );
        }

        if (
                summary
                        .getTotalDonationUsd() == null
        ) {
            summary.setTotalDonationUsd(
                    BigDecimal.ZERO
            );
        }

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberActivityDonationSummaryResponse
    getMemberActivityDonationSummary(
            Long memberId
    ) {
        Member member =
                findDetailedMember(memberId);

        validateMemberBranchAccess(
                member.getBranchId()
        );

        MemberActivityDonationSummaryResponse summary =
                memberDetailSummaryRepository
                        .summarizeMemberActivityDonations(
                                memberId
                        );

        if (summary == null) {
            return MemberActivityDonationSummaryResponse
                    .builder()
                    .donationCount(0)
                    .totalDonationKhr(BigDecimal.ZERO)
                    .totalDonationUsd(BigDecimal.ZERO)
                    .materialDonationCount(0)
                    .bankPaymentCount(0)
                    .build();
        }

        if (summary.getTotalDonationKhr() == null) {
            summary.setTotalDonationKhr(
                    BigDecimal.ZERO
            );
        }

        if (summary.getTotalDonationUsd() == null) {
            summary.setTotalDonationUsd(
                    BigDecimal.ZERO
            );
        }

        return summary;
    }


    /*
     * ==========================================================
     * UPDATE MEMBER PROFILE PHOTO BY FILE ID
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberDetailResponse
    updateMemberProfilePhoto(
            Long memberId,
            UpdateMemberProfilePhotoRequest request
    ) {

        Member member =
                memberRepository
                        .findDetailedById(
                                memberId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Member not found"
                                )
                        );

        validateMemberBranchAccess(
                member.getBranchId()
        );

        FileEntity profilePhoto =
                fileRepository
                        .findById(
                                request
                                        .profilePhotoId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Profile photo file not found"
                                )
                        );

        String mimeType =
                profilePhoto
                        .getMimeType();

        if (
                mimeType == null
                        || !mimeType
                        .toLowerCase()
                        .startsWith(
                                "image/"
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Profile photo must be an image file"
            );
        }

        member.setProfilePhoto(
                profilePhoto
        );

        Member savedMember =
                memberRepository
                        .save(
                                member
                        );

        return toMemberDetailResponse(
                savedMember
        );
    }


    /*
     * ==========================================================
     * UPLOAD MEMBER PROFILE PHOTO
     * ==========================================================
     */

    @Override
    @Transactional
    public MemberDetailResponse
    uploadMemberProfilePhoto(
            Long memberId,
            MultipartFile file
    ) {

        memberAccessValidator
                .validateAccessibleMember(
                        memberId
                );

        Member member =
                findDetailedMember(
                        memberId
                );

        FileEntity uploadedFile =
                fileService
                        .uploadFileEntity(
                                file
                        );

        member.setProfilePhoto(
                uploadedFile
        );

        Member savedMember =
                memberRepository
                        .saveAndFlush(
                                member
                        );

        return toMemberDetailResponse(
                savedMember
        );
    }


    /*
     * ==========================================================
     * ACCOUNT CREATION
     * ==========================================================
     */

    private void createPendingUserAccount(
            Member member,
            UserRole requestedRole
    ) {

        if (
                member == null
                        || member.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Saved member could not be resolved"
            );
        }

        String phone =
                trimToNull(
                        member.getPhone()
                );

        String email =
                normalizeEmail(
                        member.getEmail()
                );

        if (phone == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone is required to create a user account"
            );
        }

        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required for account activation"
            );
        }

        if (requestedRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User role is required"
            );
        }

        if (
                userRepository
                        .findByMemberId(
                                member.getId()
                        )
                        .isPresent()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has a user account"
            );
        }

        if (
                userRepository
                        .findByPhone(
                                phone
                        )
                        .isPresent()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user account already exists with phone: "
                            + phone
            );
        }

        if (
                userRepository
                        .findByEmail(
                                email
                        )
                        .isPresent()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user account already exists with email: "
                            + email
            );
        }

        String unusablePasswordHash =
                passwordEncoder
                        .encode(
                                UUID.randomUUID()
                                        .toString()
                        );

        User pendingUser =
                User.builder()

                        .memberId(
                                member.getId()
                        )

                        .branchId(
                                member.getBranchId()
                        )

                        .phone(
                                phone
                        )

                        .email(
                                email
                        )

                        .passwordHash(
                                unusablePasswordHash
                        )

                        .role(
                                requestedRole
                        )

                        .status(
                                UserStatus
                                        .PENDING_ACTIVATION
                        )

                        .fullNameKm(
                                member.getFullNameKm()
                        )

                        .fullNameEn(
                                member.getFullNameEn()
                        )

                        .failedLoginCount(
                                0
                        )

                        .build();

        userRepository
                .saveAndFlush(
                        pendingUser
                );
    }


    /*
     * ==========================================================
     * SYNCHRONIZE LINKED USER ACCOUNT
     * ==========================================================
     */

    private void synchronizeLinkedUserAccount(
            Member member
    ) {

        userRepository
                .findByMemberId(
                        member.getId()
                )
                .ifPresent(
                        user -> {

                            user.setPhone(
                                    member.getPhone()
                            );

                            user.setEmail(
                                    member.getEmail()
                            );

                            user.setFullNameKm(
                                    member.getFullNameKm()
                            );

                            user.setFullNameEn(
                                    member.getFullNameEn()
                            );

                            user.setBranchId(
                                    member.getBranchId()
                            );

                            userRepository
                                    .saveAndFlush(
                                            user
                                    );
                        }
                );
    }


    /*
     * ==========================================================
     * ROLE ASSIGNMENT
     * ==========================================================
     */

    /**
     * A position with a mapped role (BRANCH_LEADER / SECRETARY / MEMBER)
     * always drives the assigned role, overriding any explicitly requested
     * role — this is the "auto load the role" behavior the position picker
     * provides. Falls back to the requested role, or MEMBER, when the
     * position has no mapped role (e.g. "Support") or no position was
     * chosen at all.
     */
    private UserRole resolveRequestedRole(
            Position position,
            UserRole requestedRole
    ) {

        if (position != null
                && position.getMappedRole() != null) {

            return UserRole.valueOf(
                    position.getMappedRole()
            );
        }

        return requestedRole == null
                ? UserRole.MEMBER
                : requestedRole;
    }


    private void validateAssignableRole(
            UserRole actorRole,
            UserRole requestedRole
    ) {

        if (actorRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        if (requestedRole == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested role is required"
            );
        }

        boolean allowed =
                switch (actorRole) {

                    case SECRETARY ->
                            requestedRole
                                    == UserRole.MEMBER;

                    case BRANCH_LEADER ->
                            requestedRole
                                    == UserRole.MEMBER
                                    || requestedRole
                                    == UserRole.SECRETARY;

                    case ADMIN ->
                            requestedRole
                                    == UserRole.MEMBER
                                    || requestedRole
                                    == UserRole.SECRETARY
                                    || requestedRole
                                    == UserRole.BRANCH_LEADER;

                    default ->
                            false;
                };

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to assign role: "
                            + requestedRole
            );
        }
    }


    /*
     * ==========================================================
     * CURRENT USER
     * ==========================================================
     */

    private User getCurrentUser() {

        User principalUser =
                SecurityUtil
                        .getCurrentUser();

        if (
                principalUser == null
                        || principalUser
                        .getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return userRepository
                .findById(
                        principalUser
                                .getId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );
    }


    private Long getCurrentUserId() {

        return getCurrentUser()
                .getId();
    }


    /*
     * ==========================================================
     * BRANCH ACCESS
     * ==========================================================
     */

    private void validateMemberBranchAccess(
            Long requestedBranchId
    ) {

        if (requestedBranchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        User currentUser =
                getCurrentUser();

        UserRole role =
                currentUser
                        .getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        if (
                role == UserRole.ADMIN
        ) {
            return;
        }

        if (
                role != UserRole.SECRETARY
                        && role != UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to manage members"
            );
        }

        Long currentMemberId =
                currentUser
                        .getMemberId();

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

        if (
                accessibleBranchIds
                        .isEmpty()
        ) {

            Member currentMember =
                    memberRepository
                            .findById(
                                    currentMemberId
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.FORBIDDEN,
                                            "Linked member record was not found"
                                    )
                            );

            if (
                    currentMember
                            .getBranchId() != null
            ) {
                accessibleBranchIds
                        .add(
                                currentMember
                                        .getBranchId()
                        );
            }
        }

        if (
                !accessibleBranchIds
                        .contains(
                                requestedBranchId
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this branch"
            );
        }
    }


    private Long resolveMemberListBranchId(
            Long requestedBranchId
    ) {

        User currentUser =
                getCurrentUser();

        UserRole role =
                currentUser
                        .getRole();

        if (
                role == UserRole.ADMIN
        ) {
            return requestedBranchId;
        }

        if (
                role != UserRole.SECRETARY
                        && role != UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to list members"
            );
        }

        if (
                currentUser
                        .getMemberId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is not linked to a member record"
            );
        }

        Member currentMember =
                memberRepository
                        .findById(
                                currentUser
                                        .getMemberId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Linked member record was not found"
                                )
                        );

        Long homeBranchId =
                currentMember
                        .getBranchId();

        /*
         * A secretary/branch-leader can be staff of MORE than one branch
         * (see branch_staff) — not just their own member record's home
         * branch. This previously only ever checked the home branch here,
         * which incorrectly forbade a multi-branch staff member from
         * listing members of their OTHER assigned branch (e.g. from the
         * activity-donation module's branch picker). Mirrors
         * validateMemberBranchAccess, which already resolves branch access
         * this same branch_staff-plus-home-fallback way.
         */
        Set<Long> accessibleBranchIds =
                new LinkedHashSet<>(
                        branchStaffRepository
                                .findActiveBranchIdsByMemberId(
                                        currentUser.getMemberId()
                                )
                );

        if (homeBranchId != null) {
            accessibleBranchIds.add(homeBranchId);
        }

        if (accessibleBranchIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your member record is not assigned to a branch"
            );
        }

        if (requestedBranchId != null) {
            if (!accessibleBranchIds.contains(requestedBranchId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot access members from another branch"
                );
            }

            return requestedBranchId;
        }

        /*
         * No explicit branchId requested — default to the home branch
         * (a single-branch context), same as before. A caller that needs a
         * specific non-home assigned branch's members must pass branchId
         * explicitly.
         */
        if (homeBranchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your member record is not assigned to a branch"
            );
        }

        return homeBranchId;
    }


    /*
     * ==========================================================
     * MEMBER DETAIL RESPONSE
     * ==========================================================
     *
     * Member stores branchId only.
     *
     * Load the actual Branch here and give both
     * Member + Branch to MemberMapper so the API
     * can return:
     *
     * branch_id
     * branch_code
     * branch_name_km
     * branch_name_en
     */

    private MemberDetailResponse
    toMemberDetailResponse(
            Member member
    ) {
        if (member == null) {
            return null;
        }

        Branch branch = null;

        if (member.getBranchId() != null) {
            branch =
                    branchRepository
                            .findById(
                                    member.getBranchId()
                            )
                            .orElse(null);
        }

        UserRole role =
                userRepository
                        .findByMemberId(
                                member.getId()
                        )
                        .map(
                                User::getRole
                        )
                        .orElse(null);

        return memberMapper
                .toDetailResponse(
                        member,
                        branch,
                        role
                );
    }


    /*
     * ==========================================================
     * FIND ENTITIES
     * ==========================================================
     */

    private Member findDetailedMember(
            Long id
    ) {

        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member ID is required"
            );
        }

        return memberRepository
                .findDetailedById(
                        id
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + id
                        )
                );
    }


    private Branch findBranch(
            Long branchId
    ) {

        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        return branchRepository
                .findById(
                        branchId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Branch not found with ID: "
                                        + branchId
                        )
                );
    }


    private MemberStatus findStatus(
            Short id
    ) {

        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member status ID is required"
            );
        }

        return memberStatusRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member status not found with ID: "
                                        + id
                        )
                );
    }


    private MemberLevel findLevel(
            Short id
    ) {

        if (id == null) {
            return null;
        }

        return memberLevelRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member level not found with ID: "
                                        + id
                        )
                );
    }


    private Religion findReligion(
            Short id
    ) {

        if (id == null) {
            return null;
        }

        return religionRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Religion not found with ID: "
                                        + id
                        )
                );
    }


    private Ethnicity findEthnicity(
            Short id
    ) {

        if (id == null) {
            return null;
        }

        return ethnicityRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ethnicity not found with ID: "
                                        + id
                        )
                );
    }


    private Nationality resolveNationality(
            Short nationalityId
    ) {

        if (nationalityId == null) {
            return null;
        }

        return nationalityService
                .getActiveNationalityEntityById(
                        nationalityId
                );
    }


    private FileEntity findFile(
            Long id,
            String fieldName
    ) {

        if (id == null) {
            return null;
        }

        return fileRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                fieldName
                                        + " not found with ID: "
                                        + id
                        )
                );
    }


    /*
     * ==========================================================
     * SERVER-CONTROLLED MEMBER FIELDS
     * ==========================================================
     */

    private String generateMemberNo() {

        String latestMemberNo =
                memberRepository
                        .findLatestGeneratedMemberNo()
                        .orElse(
                                null
                        );

        int nextSequence =
                1;

        if (latestMemberNo != null) {

            int separatorIndex =
                    latestMemberNo
                            .lastIndexOf(
                                    '-'
                            );

            if (
                    separatorIndex >= 0
                            && separatorIndex
                            < latestMemberNo
                            .length() - 1
            ) {

                String sequenceText =
                        latestMemberNo
                                .substring(
                                        separatorIndex
                                                + 1
                                );

                try {

                    nextSequence =
                            Integer.parseInt(
                                    sequenceText
                            ) + 1;

                } catch (
                        NumberFormatException ignored
                ) {

                    nextSequence =
                            1;
                }
            }
        }

        return "TNAL-M-"
                + String.format(
                Locale.ROOT,
                "%04d",
                nextSequence
        );
    }


    /*
     * ==========================================================
     * UNIQUE VALIDATION
     * ==========================================================
     */

    private void validateUniqueValues(
            String memberNo,
            String phone,
            String email,
            Long currentId
    ) {

        boolean duplicateMemberNo;

        if (currentId == null) {

            duplicateMemberNo =
                    memberRepository
                            .existsByMemberNoIgnoreCase(
                                    memberNo
                            );

        } else {

            duplicateMemberNo =
                    memberRepository
                            .existsByMemberNoIgnoreCaseAndIdNot(
                                    memberNo,
                                    currentId
                            );
        }

        if (duplicateMemberNo) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Member number already exists: "
                            + memberNo
            );
        }

        if (phone != null) {

            boolean duplicatePhone;

            if (currentId == null) {

                duplicatePhone =
                        memberRepository
                                .existsByPhone(
                                        phone
                                );

            } else {

                duplicatePhone =
                        memberRepository
                                .existsByPhoneAndIdNot(
                                        phone,
                                        currentId
                                );
            }

            if (duplicatePhone) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Phone number already exists: "
                                + phone
                );
            }
        }

        if (email != null) {

            boolean duplicateEmail;

            if (currentId == null) {

                duplicateEmail =
                        memberRepository
                                .existsByEmailIgnoreCase(
                                        email
                                );

            } else {

                duplicateEmail =
                        memberRepository
                                .existsByEmailIgnoreCaseAndIdNot(
                                        email,
                                        currentId
                                );
            }

            if (duplicateEmail) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already exists: "
                                + email
                );
            }
        }
    }


    /*
     * ==========================================================
     * TEXT NORMALIZATION
     * ==========================================================
     */

    private String normalizeRequired(
            String value,
            String fieldName
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName
                            + " is required"
            );
        }

        return value
                .trim();
    }


    private String normalizeEmail(
            String email
    ) {

        String normalizedEmail =
                trimToNull(
                        email
                );

        if (normalizedEmail == null) {
            return null;
        }

        return normalizedEmail
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private String trimToNull(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String trimmedValue =
                value
                        .trim();

        return trimmedValue
                .isEmpty()
                ? null
                : trimmedValue;
    }


    private String normalizeTshirtSize(
            String value
    ) {

        String normalized =
                trimToNull(
                        value
                );

        if (normalized == null) {
            return null;
        }

        normalized =
                normalized
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                !ALLOWED_TSHIRT_SIZES
                        .contains(
                                normalized
                        )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    T-shirt size must be one of:
                    XS, S, M, L, XL, 2XL, or 3XL
                    """
            );
        }

        return normalized;
    }


    /*
     * ==========================================================
     * DATABASE ERRORS
     * ==========================================================
     */

    private ResponseStatusException
    createDatabaseException(
            String defaultMessage,
            DataIntegrityViolationException exception
    ) {

        String message =
                getDatabaseErrorMessage(
                        defaultMessage,
                        exception
                );

        HttpStatus status =
                determineDatabaseErrorStatus(
                        message
                );

        return new ResponseStatusException(
                status,
                message,
                exception
        );
    }


    private String getDatabaseErrorMessage(
            String defaultMessage,
            DataIntegrityViolationException exception
    ) {

        Throwable mostSpecificCause =
                exception
                        .getMostSpecificCause();

        if (
                mostSpecificCause == null
                        || mostSpecificCause
                        .getMessage() == null
                        || mostSpecificCause
                        .getMessage()
                        .isBlank()
        ) {
            return defaultMessage;
        }

        return mostSpecificCause
                .getMessage();
    }


    private HttpStatus determineDatabaseErrorStatus(
            String message
    ) {

        if (message == null) {
            return HttpStatus
                    .BAD_REQUEST;
        }

        String normalizedMessage =
                message
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (
                normalizedMessage
                        .contains(
                                "duplicate key"
                        )
                        || normalizedMessage
                        .contains(
                                "unique constraint"
                        )
        ) {
            return HttpStatus
                    .CONFLICT;
        }

        return HttpStatus
                .BAD_REQUEST;
    }


    /*
     * ==========================================================
     * ACCESSIBLE BRANCH IDS
     * ==========================================================
     */

    private Set<Long>
    getAccessibleBranchIds() {

        User currentUser =
                getCurrentUser();

        UserRole role =
                currentUser
                        .getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        /*
         * Empty set means unrestricted for ADMIN.
         */
        if (
                role == UserRole.ADMIN
        ) {
            return Set.of();
        }

        if (
                role != UserRole.SECRETARY
                        && role != UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view members"
            );
        }

        Long currentMemberId =
                currentUser
                        .getMemberId();

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

        if (
                accessibleBranchIds
                        .isEmpty()
        ) {

            Member currentMember =
                    memberRepository
                            .findById(
                                    currentMemberId
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.FORBIDDEN,
                                            "Linked member record was not found"
                                    )
                            );

            if (
                    currentMember
                            .getBranchId() != null
            ) {
                accessibleBranchIds
                        .add(
                                currentMember
                                        .getBranchId()
                        );
            }
        }

        return accessibleBranchIds;
    }
}
