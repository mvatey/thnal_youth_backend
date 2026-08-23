package org.example.tnal_youth_backend.myaccount.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.authentication.repository.RefreshTokenRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.example.tnal_youth_backend.member.education.service.MemberEducationService;
import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.example.tnal_youth_backend.member.language.service.MemberLanguageService;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationPageResponse;
import org.example.tnal_youth_backend.member.participation.service.MemberParticipationService;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;
import org.example.tnal_youth_backend.member.password.service.MemberPasswordService;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;
import org.example.tnal_youth_backend.member.personalinfo.service.MemberPersonalInfoService;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.example.tnal_youth_backend.member.politicalaffiliation.service.MemberPoliticalAffiliationService;
import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;
import org.example.tnal_youth_backend.member.skill.service.MemberSkillService;
import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;
import org.example.tnal_youth_backend.member.workhistory.service.MemberWorkHistoryService;
import org.example.tnal_youth_backend.myaccount.dto.request.ChangeMyEmailRequest;
import org.example.tnal_youth_backend.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.myaccount.dto.request.UpdateMyPersonalInfoRequest;
import org.example.tnal_youth_backend.myaccount.security.CurrentMemberResolver;
import org.example.tnal_youth_backend.myaccount.service.MyAccountService;
import org.example.tnal_youth_backend.member.family.dto.request.MemberFamilyInfoRequest;
import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyInfoResponse;
import org.example.tnal_youth_backend.member.family.service.MemberFamilyService;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyAccountServiceImpl
        implements MyAccountService {

    private final CurrentMemberResolver
            currentMemberResolver;

    private final MemberService
            memberService;

    private final MemberFamilyService
            memberFamilyService;

    private final MemberWorkHistoryService
            memberWorkHistoryService;

    private final MemberEducationService
            memberEducationService;

    private final MemberPersonalInfoService
            memberPersonalInfoService;

    private final MemberLanguageService
            memberLanguageService;

    private final MemberSkillService
            memberSkillService;

    private final MemberPoliticalAffiliationService
            memberPoliticalAffiliationService;

    private final MemberPasswordService
            memberPasswordService;

    private final MemberParticipationService
            memberParticipationService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * The currently-authenticated login account, freshly loaded from the
     * database. Used by the password/email self-service methods below,
     * which operate on the account directly instead of through a member
     * record — see changeMyPassword/changeMyEmail.
     */
    private User getCurrentUserEntity() {
        var principal = SecurityUtil.getCurrentUser();

        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMyProfile() {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberService
                .getMemberById(memberId);
    }

    /*
     * Reuses MemberService.uploadMemberProfilePhoto(), the same
     * upload/storage path staff use to change a member's photo.
     * MemberAccessValidator already allows a MEMBER-role user to
     * access only their own member record, so this call is safely
     * scoped to the authenticated member without any extra checks.
     */
    @Override
    @Transactional
    public MemberDetailResponse uploadMyProfilePhoto(
            MultipartFile file
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberService
                .uploadMemberProfilePhoto(
                        memberId,
                        file
                );
    }


    @Override
    @Transactional(readOnly = true)
    public MemberPersonalInfoResponse
    getMyPersonalInfo() {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberPersonalInfoService
                .getPersonalInfo(
                        memberId
                );
    }

    @Override
    @Transactional
    public MemberPersonalInfoResponse
    updateMyPersonalInfo(
            UpdateMyPersonalInfoRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberPersonalInfoService
                .updateMyPersonalInfo(
                        memberId,
                        request
                );
    }

    @Override
    @Transactional
    public MemberPersonalInfoResponse
    uploadMyCv(
            MultipartFile file
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberPersonalInfoService
                .uploadCv(
                        memberId,
                        file
                );
    }


    @Override
    @Transactional(readOnly = true)
    public MemberFamilyInfoResponse
    getMyFamilyInfo() {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberFamilyService
                .getFamilyInfo(memberId);
    }

    @Override
    @Transactional
    public MemberFamilyInfoResponse
    updateMyFamilyInfo(
            MemberFamilyInfoRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberFamilyService
                .updateFamilyInfo(
                        memberId,
                        request
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberWorkHistoryResponse>
    getMyWorkHistory() {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberWorkHistoryService
                .getByMemberId(memberId);
    }

    @Override
    @Transactional
    public MemberWorkHistoryResponse
    createMyWorkHistory(
            MemberWorkHistoryRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberWorkHistoryService.create(
                memberId,
                request
        );
    }

    @Override
    @Transactional
    public MemberWorkHistoryResponse
    updateMyWorkHistory(
            Long workId,
            MemberWorkHistoryRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberWorkHistoryService.update(
                memberId,
                workId,
                request
        );
    }

    @Override
    @Transactional
    public void deleteMyWorkHistory(
            Long workId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        memberWorkHistoryService.delete(
                memberId,
                workId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberEducationResponse>
    getMyEducation() {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberEducationService
                .getByMemberId(
                        memberId
                );
    }

    @Override
    @Transactional
    public MemberEducationResponse
    createMyEducation(
            MemberEducationRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberEducationService.create(
                memberId,
                request
        );
    }

    @Override
    @Transactional
    public MemberEducationResponse
    updateMyEducation(
            Long educationId,
            MemberEducationRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberEducationService.update(
                memberId,
                educationId,
                request
        );
    }

    @Override
    @Transactional
    public void deleteMyEducation(
            Long educationId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        memberEducationService.delete(
                memberId,
                educationId
        );
    }

    @Override
    @Transactional
    public MemberEducationResponse
    uploadMyEducationCertificate(
            Long educationId,
            MultipartFile file
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberEducationService
                .uploadCertificate(
                        memberId,
                        educationId,
                        file
                );
    }

    @Override
    public List<MemberLanguageResponse>
    getMyLanguages() {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberLanguageService
                .getByMemberId(
                        memberId
                );
    }

    @Override
    @Transactional
    public MemberLanguageResponse
    createMyLanguage(
            MemberLanguageRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberLanguageService
                .create(
                        memberId,
                        request
                );
    }

    @Override
    @Transactional
    public MemberLanguageResponse
    updateMyLanguage(
            Long languageId,
            MemberLanguageRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberLanguageService
                .update(
                        memberId,
                        languageId,
                        request
                );
    }

    @Override
    @Transactional
    public void deleteMyLanguage(
            Long languageId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        memberLanguageService
                .delete(
                        memberId,
                        languageId
                );
    }

    @Override
    @Transactional
    public MemberLanguageResponse
    uploadMyLanguageCertificate(
            Long languageId,
            MultipartFile file
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberLanguageService
                .uploadCertificate(
                        memberId,
                        languageId,
                        file
                );
    }

    @Override
    @Transactional
    public MemberLanguageResponse
    removeMyLanguageCertificate(
            Long languageId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberLanguageService
                .removeCertificate(
                        memberId,
                        languageId
                );
    }

    @Override
    public List<MemberSkillResponse>
    getMySkills() {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberSkillService
                .getByMemberId(
                        memberId
                );
    }

    @Override
    @Transactional
    public MemberSkillResponse
    createMySkill(
            MemberSkillRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberSkillService
                .create(
                        memberId,
                        request
                );
    }

    @Override
    @Transactional
    public MemberSkillResponse
    updateMySkill(
            Long skillId,
            MemberSkillRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberSkillService
                .update(
                        memberId,
                        skillId,
                        request
                );
    }

    @Override
    @Transactional
    public void deleteMySkill(
            Long skillId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        memberSkillService
                .delete(
                        memberId,
                        skillId
                );
    }

    @Override
    @Transactional
    public MemberSkillResponse
    uploadMySkillCertificate(
            Long skillId,
            MultipartFile file
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberSkillService
                .uploadCertificate(
                        memberId,
                        skillId,
                        file
                );
    }

    @Override
    @Transactional
    public MemberSkillResponse
    removeMySkillCertificate(
            Long skillId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberSkillService
                .removeCertificate(
                        memberId,
                        skillId
                );
    }

    @Override
    public List<MemberPoliticalAffiliationResponse>
    getMyPoliticalAffiliations() {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberPoliticalAffiliationService
                .getByMemberId(
                        memberId
                );
    }

    @Override
    public MemberPoliticalAffiliationResponse
    getMyPoliticalAffiliation(
            Long affiliationId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberPoliticalAffiliationService
                .getById(
                        memberId,
                        affiliationId
                );
    }

    @Override
    @Transactional
    public MemberPoliticalAffiliationResponse
    createMyPoliticalAffiliation(
            MemberPoliticalAffiliationRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberPoliticalAffiliationService
                .create(
                        memberId,
                        request
                );
    }

    @Override
    @Transactional
    public MemberPoliticalAffiliationResponse
    updateMyPoliticalAffiliation(
            Long affiliationId,
            MemberPoliticalAffiliationRequest request
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberPoliticalAffiliationService
                .update(
                        memberId,
                        affiliationId,
                        request
                );
    }

    @Override
    @Transactional
    public void deleteMyPoliticalAffiliation(
            Long affiliationId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        memberPoliticalAffiliationService
                .delete(
                        memberId,
                        affiliationId
                );
    }

    @Override
    @Transactional
    public MemberPasswordStatusResponse changeMyPassword(
            ChangeMyPasswordRequest request
    ) {
        User currentUser = getCurrentUserEntity();

        return resetCurrentUserPassword(
                currentUser,
                request.oldPassword(),
                request.newPassword(),
                request.confirmPassword()
        );
    }

    /*
     * This is self-service: the account holder changing their own
     * password, so the old password is always required to prove they
     * still know it — unlike /members/{id}/account/password, where
     * staff reset a DIFFERENT member's password and naturally can't
     * supply their old one.
     */
    private MemberPasswordStatusResponse resetCurrentUserPassword(
            User user,
            String oldPassword,
            String newPassword,
            String confirmPassword
    ) {
        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The account must complete activation before resetting its password"
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only an active account can reset its password"
            );
        }

        if (oldPassword == null || oldPassword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Old password is required"
            );
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Old password is incorrect"
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password is required"
            );
        }

        if (newPassword.length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must contain at least 6 characters"
            );
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password confirmation is required"
            );
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password confirmation does not match"
            );
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must be different from the current password"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        User savedUser = userRepository.saveAndFlush(user);

        /*
         * Revoke existing refresh tokens after password reset.
         * The current access token may remain valid until its normal expiry.
         */
        refreshTokenRepository.deleteByUser(savedUser);

        return new MemberPasswordStatusResponse(
                savedUser.getMemberId(),
                savedUser.getId(),
                true,
                savedUser.getActivatedAt() != null,
                savedUser.getPhone(),
                savedUser.getEmail(),
                savedUser.getRole() != null ? savedUser.getRole().name() : null,
                savedUser.getStatus() != null ? savedUser.getStatus().name() : null,
                savedUser.getActivatedAt(),
                savedUser.getLastLoginAt()
        );
    }

    @Override
    @Transactional
    public void changeMyEmail(
            ChangeMyEmailRequest request
    ) {
        User currentUser = getCurrentUserEntity();

        String newEmail = request.newEmail().trim().toLowerCase();

        if (newEmail.equalsIgnoreCase(currentUser.getEmail())) {
            return;
        }

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                newEmail,
                currentUser.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already used by another account"
            );
        }

        currentUser.setEmail(newEmail);

        userRepository.saveAndFlush(currentUser);
    }

    @Override
    public MemberParticipationPageResponse
    getMyParticipations(
            int page,
            int size,
            String search,
            Short typeId,
            Short attendanceStatusId
    ) {
        Long memberId =
                currentMemberResolver
                        .getCurrentMemberId();

        return memberParticipationService
                .getParticipationsByMemberId(
                        memberId,
                        page,
                        size,
                        search,
                        typeId,
                        attendanceStatusId
                );
    }
}
