package org.example.tnal_youth_backend.member.member.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.level.repository.MemberLevelRepository;
import org.example.tnal_youth_backend.member.member.dto.request.CreateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.request.UpdateMemberRequest;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberListResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

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

    private final MemberStatusRepository
            memberStatusRepository;

    private final MemberLevelRepository
            memberLevelRepository;

    private final ReligionRepository religionRepository;

    private final FileRepository fileRepository;

    private final MemberMapper memberMapper;

    /*
     * ==========================================================
     * GET ALL MEMBERS
     * ==========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public List<MemberListResponse> getAllMembers() {

        return memberRepository
                .findAllDetailed()
                .stream()
                .map(memberMapper::toListResponse)
                .toList();
    }

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

        /*
         * Your current Gender enum does not contain MONK.
         *
         * It contains:
         *
         * MALE
         * FEMALE
         * OTHER
         *
         * Therefore, the Monk card currently counts
         * members stored with Gender.OTHER.
         */
        long monkMembers =
                memberRepository.countByGender(
                        Gender.OTHER
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
        String memberNo =
                normalizeRequired(
                        request.memberNo(),
                        "Member number"
                );

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

                        .branchId(
                                request.branchId()
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
                                request.createdById()
                        )

                        .build();

        try {
            Member savedMember =
                    memberRepository.saveAndFlush(
                            member
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
        Member member =
                findDetailedMember(id);

        String memberNo =
                normalizeRequired(
                        request.memberNo(),
                        "Member number"
                );

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

        member.setMemberNo(
                memberNo
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
                request.branchId()
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
                    "Member could not be updated",
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
}