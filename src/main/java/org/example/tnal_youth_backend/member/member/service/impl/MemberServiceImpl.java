package org.example.tnal_youth_backend.member.member.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchStaffRepository;
import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.level.repository.MemberLevelRepository;
import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberStatusRequest;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberListResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberPageResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberSummaryResponse;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.mapper.MemberMapper;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.service.MemberService;
import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.religion.repository.ReligionRepository;
import org.example.tnal_youth_backend.member.status.entity.MemberStatus;
import org.example.tnal_youth_backend.member.status.repository.MemberStatusRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;


import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl
        implements MemberService {

    private static final String BUDDHISM_CODE =
            "BUDDHISM";

    private static final String ISLAM_CODE =
            "ISLAM";

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final MemberStatusRepository
            memberStatusRepository;

    private final MemberLevelRepository
            memberLevelRepository;

    private final ReligionRepository religionRepository;

    private final FileRepository fileRepository;

    private final MemberMapper memberMapper;

    private final BranchRepository branchRepository;

    private final BranchStaffRepository branchStaffRepository;

    /*
     * ==========================================================
     * GET MEMBER SUMMARY
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public MemberSummaryResponse getMemberSummary() {

        long maleMembers =
                memberRepository.countByGender(
                        Gender.MALE
                );

        long femaleMembers =
                memberRepository.countByGender(
                        Gender.FEMALE
                );
        
        long monkMembers =
                memberRepository.countByGender(
                        Gender.MONK
                );

        long buddhistMembers =
                memberRepository.countByReligionCode(
                        BUDDHISM_CODE
                );

        long islamMembers =
                memberRepository.countByReligionCode(
                        ISLAM_CODE
                );

        return new MemberSummaryResponse(
                maleMembers,
                femaleMembers,
                monkMembers,
                buddhistMembers,
                islamMembers
        );
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
        Member member =
                findDetailedMember(id);

        validateMemberBranchAccess(
                member.getBranchId()
        );

        return memberMapper.toDetailResponse(
                member
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
                        request.branchId()
                );

        validateMemberBranchAccess(
                branch.getId()
        );

        String memberNo =
                generateMemberNo();

        String phone =
                trimToNull(request.phone());

        String email =
                normalizeEmail(request.email());

        validateUniqueValues(
                memberNo,
                phone,
                email,
                null
        );

        MemberStatus status =
                findStatus(request.statusId());

        MemberLevel level =
                findLevel(request.levelId());

        Religion religion =
                findReligion(request.religionId());

        FileEntity profilePhoto =
                findFile(
                        request.profilePhotoId(),
                        "Profile photo"
                );

        FileEntity cvFile =
                findFile(
                        request.cvFileId(),
                        "CV file"
                );

        Member member =
                Member.builder()
                        .branchId(
                                branch.getId()
                        )

                        .memberNo(memberNo)

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

                        .status(status)

                        .level(level)

                        .religion(religion)

                        .gender(
                                request.gender()
                        )

                        .dateOfBirth(
                                request.dateOfBirth()
                        )

                        .placeOfBirth(
                                trimToNull(
                                        request.placeOfBirth()
                                )
                        )

                        .phone(phone)

                        .email(email)

                        .currentAddress(
                                trimToNull(
                                        request.currentAddress()
                                )
                        )

                        .permanentAddress(
                                trimToNull(
                                        request.permanentAddress()
                                )
                        )

                        .profilePhoto(profilePhoto)

                        .cvFile(cvFile)

                        .joinedOn(
                                request.joinedOn()
                        )

                        .bio(
                                trimToNull(
                                        request.bio()
                                )
                        )

                        .createdById(
                                getCurrentUserId()
                        )

                        .build();

        try {
            Member savedMember =
                    memberRepository.saveAndFlush(
                            member
                    );

            createPendingUserAccount(
                    savedMember
            );

            Member detailedMember =
                    findDetailedMember(
                            savedMember.getId()
                    );

            return memberMapper.toDetailResponse(
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
        Branch branch =
                findBranch(request.branchId());

        validateMemberBranchAccess(
                branch.getId()
        );

        Member member =
                findDetailedMember(id);

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

        String memberNo =
                member.getMemberNo();

        String phone =
                trimToNull(request.phone());

        String email =
                normalizeEmail(request.email());

        validateUniqueValues(
                memberNo,
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
                    memberRepository.saveAndFlush(
                            member
                    );

            /*
             * Keep the login account email synchronized with
             * the member profile email.
             */
            userRepository
                    .findByMemberId(
                            savedMember.getId()
                    )
                    .ifPresent(user -> {
                        user.setEmail(
                                savedMember.getEmail()
                        );

                        userRepository.saveAndFlush(
                                user
                        );
                    });

            Member detailedMember =
                    findDetailedMember(id);

            return memberMapper.toDetailResponse(
                    detailedMember
            );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw createDatabaseException(
                    "You do not have access to this branch",
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
                findDetailedMember(id);

        try {
            memberRepository.delete(
                    member
            );

            memberRepository.flush();

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
     * FIND MEMBER
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
                .findDetailedById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member not found with ID: "
                                        + id
                        )
                );
    }

    /*
     * ==========================================================
     * FIND LOOKUP VALUES
     * ==========================================================
     */

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
                .findById(id)
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
                .findById(id)
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
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Religion not found with ID: "
                                        + id
                        )
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
                .findById(id)
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
                        .orElse(null);

        int nextSequence = 1;

        if (latestMemberNo != null) {
            int separatorIndex =
                    latestMemberNo.lastIndexOf('-');

            if (separatorIndex >= 0
                    && separatorIndex
                    < latestMemberNo.length() - 1) {

                String sequenceText =
                        latestMemberNo.substring(
                                separatorIndex + 1
                        );

                try {
                    nextSequence =
                            Integer.parseInt(
                                    sequenceText
                            ) + 1;

                } catch (
                        NumberFormatException ignored
                ) {
                    nextSequence = 1;
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

    private Long getCurrentUserId() {

        User authenticatedUser =
                SecurityUtil.getCurrentUser();

        if (authenticatedUser == null
                || authenticatedUser.getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return userRepository
                .findById(
                        authenticatedUser.getId()
                )
                .map(User::getId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
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
                                .existsByPhone(phone);
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
        if (value == null
                || value.isBlank()) {

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
        String normalizedEmail =
                trimToNull(email);

        if (normalizedEmail == null) {
            return null;
        }

        return normalizedEmail.toLowerCase(
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
                value.trim();

        return trimmedValue.isEmpty()
                ? null
                : trimmedValue;
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
                exception.getMostSpecificCause();

        if (mostSpecificCause == null
                || mostSpecificCause.getMessage() == null
                || mostSpecificCause
                .getMessage()
                .isBlank()) {

            return defaultMessage;
        }

        return mostSpecificCause.getMessage();
    }

    private HttpStatus determineDatabaseErrorStatus(
            String message
    ) {
        if (message == null) {
            return HttpStatus.BAD_REQUEST;
        }

        String normalizedMessage =
                message.toLowerCase(
                        Locale.ROOT
                );

        if (normalizedMessage.contains(
                "duplicate key"
        )
                || normalizedMessage.contains(
                "unique constraint"
        )) {

            return HttpStatus.CONFLICT;
        }

        return HttpStatus.BAD_REQUEST;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberPageResponse getMembers(
            int page,
            int size,
            String search,
            Long branchId,
            Short statusId,
            Gender gender
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

        if (branchId != null && branchId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID must be greater than zero"
            );
        }

        if (statusId != null) {
            findStatus(statusId);
        }

        String normalizedSearch =
                trimToNull(search);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        Page<Object[]> memberPage =
                memberRepository.findMemberPage(
                        normalizedSearch,
                        branchId,
                        statusId,
                        gender != null
                                ? gender.name()
                                : null,
                        pageable
                );

        List<MemberListResponse> content =
                memberPage.getContent()
                        .stream()
                        .map(memberMapper::toListResponse)
                        .toList();

        return MemberPageResponse.builder()
                .content(content)
                .page(memberPage.getNumber())
                .size(memberPage.getSize())
                .totalElements(
                        memberPage.getTotalElements()
                )
                .totalPages(
                        memberPage.getTotalPages()
                )
                .first(memberPage.isFirst())
                .last(memberPage.isLast())
                .build();
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
                .findById(branchId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Branch not found with ID: "
                                        + branchId
                        )
                );
    }
    private void validateMemberBranchAccess(
            Long requestedBranchId
    ) {
        User principalUser =
                SecurityUtil.getCurrentUser();

        if (principalUser == null
                || principalUser.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        User currentUser =
                userRepository
                        .findById(principalUser.getId())
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
         * Admin may create members in any existing branch.
         */
        if (role == UserRole.ADMIN) {
            return;
        }

        if (role != UserRole.SECRETARY
                && role != UserRole.BRANCH_LEADER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to create members"
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
         * Fallback only when branch_staff has no assignment.
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

        System.out.println(
                "MEMBER CREATE USER ID: "
                        + currentUser.getId()
        );
        System.out.println(
                "MEMBER CREATE ROLE: "
                        + role
        );
        System.out.println(
                "MEMBER CREATE MEMBER ID: "
                        + currentMemberId
        );
        System.out.println(
                "MEMBER CREATE ACCESSIBLE BRANCHES: "
                        + accessibleBranchIds
        );
        System.out.println(
                "MEMBER CREATE REQUESTED BRANCH: "
                        + requestedBranchId
        );

        if (!accessibleBranchIds.contains(
                requestedBranchId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this member."
            );
        }
    }
    @Override
    @Transactional
    public MemberDetailResponse updateMemberStatus(
            Long id,
            UpdateMemberStatusRequest request
    ) {
        Member member =
                findDetailedMember(id);

        validateMemberBranchAccess(
                member.getBranchId()
        );

        MemberStatus status =
                findStatus(
                        request.statusId()
                );

        member.setStatus(status);

        try {
            memberRepository.saveAndFlush(member);

            Member detailedMember =
                    findDetailedMember(id);

            return memberMapper.toDetailResponse(
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

    private void createPendingUserAccount(
            Member member
    ) {
        if (member == null
                || member.getId() == null) {
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

        if (userRepository
                .findByMemberId(
                        member.getId()
                )
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This member already has a user account"
            );
        }

        if (userRepository
                .findByPhone(phone)
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user account already exists with phone: "
                            + phone
            );
        }

        if (userRepository
                .findByEmail(email)
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A user account already exists with email: "
                            + email
            );
        }

        /*
         * Nobody knows this value. It only satisfies the current
         * password_hash NOT NULL database constraint.
         */
        String unusablePasswordHash =
                passwordEncoder.encode(
                        UUID.randomUUID().toString()
                );

        User pendingUser =
                User.builder()
                        .memberId(
                                member.getId()
                        )
                        .phone(phone)
                        .email(email)
                        .passwordHash(
                                unusablePasswordHash
                        )
                        .role(
                                UserRole.MEMBER
                        )
                        .status(
                                UserStatus.PENDING_ACTIVATION
                        )
                        .fullNameKm(
                                member.getFullNameKm()
                        )
                        .fullNameEn(
                                member.getFullNameEn()
                        )
                        .failedLoginCount(0)
                        .build();

        userRepository.saveAndFlush(
                pendingUser
        );
    }
}