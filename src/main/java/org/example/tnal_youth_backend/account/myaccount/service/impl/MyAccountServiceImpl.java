package org.example.tnal_youth_backend.account.myaccount.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.account.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.request.UpdateMyAccountRequest;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountSummaryResponse;
import org.example.tnal_youth_backend.account.myaccount.mapper.MyAccountMapper;
import org.example.tnal_youth_backend.account.myaccount.service.MyAccountService;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.file.service.FileService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.entity.TshirtSize;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.level.repository.MemberLevelRepository;
import org.example.tnal_youth_backend.member.status.entity.MemberStatus;
import org.example.tnal_youth_backend.member.status.repository.MemberStatusRepository;
import org.example.tnal_youth_backend.member.religion.repository.ReligionRepository;
import org.example.tnal_youth_backend.member.nationality.repository.NationalityRepository;
import org.example.tnal_youth_backend.member.ethnicity.repository.EthnicityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Service("legacyMyAccountService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyAccountServiceImpl implements MyAccountService {

    private static final Set<String> ALLOWED_CV_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;
    private final MemberStatusRepository memberStatusRepository;
    private final MemberLevelRepository memberLevelRepository;
    private final ReligionRepository religionRepository;
    private final NationalityRepository nationalityRepository;
    private final EthnicityRepository ethnicityRepository;
    private final JdbcTemplate jdbcTemplate;

    private final PasswordEncoder passwordEncoder;
    private final MyAccountMapper myAccountMapper;


    /*
     * ==========================================================
     * GET MY ACCOUNT
     * ==========================================================
     */

    @Override
    public MyAccountResponse getMyAccount() {

        User user = getCurrentUserFromDatabase();
        Member member = getLinkedMember(user);

        return myAccountMapper.toResponse(
                user,
                member
        );
    }

    /*
     * ==========================================================
     * GET MY ACCOUNT SUMMARY
     * ==========================================================
     */

    @Override
    public MyAccountSummaryResponse getMyAccountSummary() {

        User user =
                getCurrentUserFromDatabase();

        Member member =
                getLinkedMember(user);

        Long memberId =
                member.getId();

        String activitySql = """
            SELECT
                COUNT(*) FILTER (
                    WHERE UPPER(ats.code) = 'PRESENT'
                ) AS attended_activities,

                COUNT(*) FILTER (
                    WHERE UPPER(ats.code) = 'ABSENT'
                ) AS absent_activities

            FROM activity_participants ap

            LEFT JOIN attendance_statuses ats
                   ON ats.id = ap.attendance_status_id

            WHERE ap.member_id = ?
            """;

        ActivitySummary activitySummary =
                jdbcTemplate.queryForObject(
                        activitySql,
                        (
                                resultSet,
                                rowNumber
                        ) -> new ActivitySummary(
                                resultSet.getLong(
                                        "attended_activities"
                                ),
                                resultSet.getLong(
                                        "absent_activities"
                                )
                        ),
                        memberId
                );

        Long totalDonations =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM donations
                        WHERE member_id = ?
                        """,
                        Long.class,
                        memberId
                );

        return new MyAccountSummaryResponse(
                activitySummary != null
                        ? activitySummary.attendedActivities()
                        : 0L,

                activitySummary != null
                        ? activitySummary.absentActivities()
                        : 0L,

                totalDonations != null
                        ? totalDonations
                        : 0L
        );
    }

    private record ActivitySummary(

            long attendedActivities,

            long absentActivities
    ) {
    }

    /*
     * ==========================================================
     * UPDATE MY ACCOUNT
     * ==========================================================
     */

    @Override
    @Transactional
    public MyAccountResponse updateMyAccount(
            UpdateMyAccountRequest request
    ) {
        User user = getCurrentUserFromDatabase();
        Member member = getLinkedMember(user);

        String phone = normalize(request.phone());
        String email = normalizeEmail(request.email());
        String fullNameKm = normalize(request.fullNameKm());
        String fullNameEn = normalize(request.fullNameEn());

        validateRequiredPhone(phone);
        validateRequiredKhmerName(fullNameKm);

        validatePhoneIsAvailable(
                phone,
                user,
                member
        );

        validateEmailIsAvailable(
                email,
                user,
                member
        );

        /*
         * Update members.
         *
         * Member Page reads this same row, so it will immediately
         * see the changes made from My Account.
         */
        member.setPhone(phone);
        member.setEmail(email);
        member.setFullNameKm(fullNameKm);
        member.setFullNameEn(fullNameEn);
        member.setGender(request.gender());
        member.setDateOfBirth(request.dateOfBirth());
        member.setPlaceOfBirth(
                normalize(request.placeOfBirth())
        );
        member.setCurrentAddress(
                normalize(request.currentAddress())
        );
        member.setPermanentAddress(
                normalize(request.permanentAddress())
        );
        member.setBio(
                normalize(request.bio())
        );
        member.setTshirtSize(
                TshirtSize.fromValue(
                        normalize(request.tshirtSize())
                )
        );
        member.setReligion(request.religionId() == null ? null : religionRepository.findById(request.religionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Religion not found")));
        member.setNationality(request.nationalityId() == null ? null : nationalityRepository.findById(request.nationalityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nationality not found")));
        member.setEthnicity(request.ethnicityId() == null ? null : ethnicityRepository.findById(request.ethnicityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ethnicity not found")));

        validateBranch(request.branchId());

        MemberStatus memberStatus =
                findMemberStatus(request.memberStatusId());

        MemberLevel memberLevel =
                findMemberLevel(request.memberLevelId());

        member.setBranchId(request.branchId());
        member.setStatus(memberStatus);
        member.setLevel(memberLevel);
        member.setJoinedOn(request.joinedOn());

        updateProfilePhoto(
                member,
                request.profilePhotoId()
        );

        updateCvFile(
                member,
                request.cvFileId()
        );

        /*
         * Keep authentication fields synchronized.
         *
         * Login uses users.phone/users.email.
         * Sidebar and older authentication responses may still use
         * the name and profile image stored in users.
         */
        user.setPhone(phone);
        user.setEmail(email);
        user.setFullNameKm(fullNameKm);
        user.setFullNameEn(fullNameEn);

        if (member.getProfilePhoto() != null) {
            user.setProfileImage(
                    member.getProfilePhoto().getFilePath()
            );
        }

        Member savedMember = memberRepository.save(member);
        User savedUser = userRepository.save(user);

        return myAccountMapper.toResponse(
                savedUser,
                savedMember
        );
    }

    @Override
    @Transactional
    public MyAccountResponse updateProfilePhoto(
            MultipartFile file
    ) {
        User user = getCurrentUserFromDatabase();
        Member member = getLinkedMember(user);

        FileEntity uploadedPhoto = fileService.uploadImage(
                file,
                user.getId()
        );

        member.setProfilePhoto(uploadedPhoto);
        user.setProfileImage(uploadedPhoto.getFilePath());

        Member savedMember = memberRepository.save(member);
        User savedUser = userRepository.save(user);

        return myAccountMapper.toResponse(
                savedUser,
                savedMember
        );
    }

    /*
     * ==========================================================
     * CHANGE PASSWORD
     * ==========================================================
     */

    @Override
    @Transactional
    public void changeMyPassword(
            ChangeMyPasswordRequest request
    ) {
        User user = getCurrentUserFromDatabase();

        validatePasswordConfirmation(
                request.newPassword(),
                request.confirmPassword()
        );

        validateNewPasswordIsDifferent(
                request.newPassword(),
                user.getPasswordHash()
        );

        String encodedPassword =
                passwordEncoder.encode(
                        request.newPassword()
                );

        user.setPasswordHash(encodedPassword);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        userRepository.save(user);
    }

    /*
     * ==========================================================
     * CURRENT AUTHENTICATED USER
     * ==========================================================
     */

    private User getCurrentUserFromDatabase() {

        User authenticatedUser =
                SecurityUtil.getCurrentUser();

        if (authenticatedUser == null
                || authenticatedUser.getId() == null) {
            throw new IllegalStateException(
                    "Authenticated user was not found"
            );
        }

        return userRepository
                .findById(authenticatedUser.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user was not found in database"
                        )
                );
    }

    /*
     * ==========================================================
     * LINKED MEMBER
     * ==========================================================
     */

    private Member getLinkedMember(User user) {

        if (user.getMemberId() == null) {
            throw new IllegalStateException(
                    "This account is not linked to a member profile"
            );
        }

        return memberRepository
                .findDetailedById(user.getMemberId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "The member profile linked to this account "
                                        + "was not found"
                        )
                );
    }

    /*
     * ==========================================================
     * SUMMARY COUNT
     * ==========================================================
     */

    private long countRecordsByMemberId(
            String tableName,
            Long memberId
    ) {
        /*
         * tableName is not supplied by the client.
         * Only internal constant table names call this method.
         */
        String sql =
                """
                SELECT COUNT(*)
                FROM %s
                WHERE member_id = ?
                """.formatted(tableName);

        Long count =
                jdbcTemplate.queryForObject(
                        sql,
                        Long.class,
                        memberId
                );

        return count == null
                ? 0L
                : count;
    }

    /*
     * ==========================================================
     * MEMBER LOOKUPS
     * ==========================================================
     */

    private void validateBranch(
            Long branchId
    ) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Branch ID is required"
            );
        }

        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM branches
                WHERE id = ?
                """,
                Long.class,
                branchId
        );

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Branch not found with ID: " + branchId
            );
        }
    }

    private MemberStatus findMemberStatus(
            Short statusId
    ) {
        if (statusId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member status ID is required"
            );
        }

        return memberStatusRepository
                .findById(statusId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member status not found with ID: "
                                        + statusId
                        )
                );
    }

    private MemberLevel findMemberLevel(
            Short levelId
    ) {
        if (levelId == null) {
            return null;
        }

        return memberLevelRepository
                .findById(levelId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member level not found with ID: "
                                        + levelId
                        )
                );
    }

    /*
     * ==========================================================
     * PROFILE PHOTO
     * ==========================================================
     */

    private void updateProfilePhoto(
            Member member,
            Long profilePhotoId
    ) {
        /*
         * Null means that the current profile photo is unchanged.
         */
        if (profilePhotoId == null) {
            return;
        }

        FileEntity profilePhoto =
                fileRepository
                        .findById(profilePhotoId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Profile photo file was not found"
                                )
                        );

        validateProfilePhoto(profilePhoto);

        member.setProfilePhoto(profilePhoto);
    }

    private void validateProfilePhoto(
            FileEntity profilePhoto
    ) {
        String mimeType = profilePhoto.getMimeType();

        if (mimeType == null
                || !mimeType.toLowerCase(Locale.ROOT)
                .startsWith("image/")) {
            throw new IllegalArgumentException(
                    "The selected file must be an image"
            );
        }
    }

    /*
     * ==========================================================
     * CV FILE
     * ==========================================================
     */

    private void updateCvFile(
            Member member,
            Long cvFileId
    ) {
        if (cvFileId == null) {
            return;
        }

        FileEntity cvFile =
                fileRepository
                        .findById(cvFileId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "CV file was not found"
                                )
                        );

        validateCvFile(cvFile);
        member.setCvFile(cvFile);
    }

    private void validateCvFile(
            FileEntity cvFile
    ) {
        String mimeType = cvFile.getMimeType();

        if (mimeType == null
                || !ALLOWED_CV_MIME_TYPES.contains(
                mimeType.toLowerCase(Locale.ROOT)
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CV file must be PDF, DOC, or DOCX"
            );
        }
    }

    /*
     * ==========================================================
     * REQUIRED FIELD VALIDATION
     * ==========================================================
     */

    private void validateRequiredPhone(String phone) {

        if (phone == null) {
            throw new IllegalArgumentException(
                    "Phone number is required"
            );
        }
    }

    private void validateRequiredKhmerName(
            String fullNameKm
    ) {
        if (fullNameKm == null) {
            throw new IllegalArgumentException(
                    "Khmer full name is required"
            );
        }
    }

    /*
     * ==========================================================
     * PHONE VALIDATION
     * ==========================================================
     */

    private void validatePhoneIsAvailable(
            String phone,
            User currentUser,
            Member currentMember
    ) {
        boolean usedByAnotherUser =
                userRepository.existsByPhoneAndIdNot(
                        phone,
                        currentUser.getId()
                );

        if (usedByAnotherUser) {
            throw new IllegalArgumentException(
                    "Phone number is already used by another account"
            );
        }

        boolean usedByAnotherMember =
                memberRepository.existsByPhoneAndIdNot(
                        phone,
                        currentMember.getId()
                );

        if (usedByAnotherMember) {
            throw new IllegalArgumentException(
                    "Phone number is already used by another member"
            );
        }
    }

    /*
     * ==========================================================
     * EMAIL VALIDATION
     * ==========================================================
     */

    private void validateEmailIsAvailable(
            String email,
            User currentUser,
            Member currentMember
    ) {
        if (email == null) {
            return;
        }

        boolean usedByAnotherUser =
                userRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                email,
                                currentUser.getId()
                        );

        if (usedByAnotherUser) {
            throw new IllegalArgumentException(
                    "Email is already used by another account"
            );
        }

        boolean usedByAnotherMember =
                memberRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                email,
                                currentMember.getId()
                        );

        if (usedByAnotherMember) {
            throw new IllegalArgumentException(
                    "Email is already used by another member"
            );
        }
    }

    /*
     * ==========================================================
     * PASSWORD VALIDATION
     * ==========================================================
     */

    private void validatePasswordConfirmation(
            String newPassword,
            String confirmPassword
    ) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "New password and confirmation do not match"
            );
        }
    }

    private void validateNewPasswordIsDifferent(
            String newPassword,
            String currentPasswordHash
    ) {
        if (currentPasswordHash != null
                && passwordEncoder.matches(
                newPassword,
                currentPasswordHash
        )) {
            throw new IllegalArgumentException(
                    "New password must be different "
                            + "from the current password"
            );
        }
    }

    /*
     * ==========================================================
     * NORMALIZATION
     * ==========================================================
     */

    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeEmail(String email) {

        String normalizedEmail = normalize(email);

        if (normalizedEmail == null) {
            return null;
        }

        return normalizedEmail.toLowerCase(
                Locale.ROOT
        );
    }
}
