package org.example.tnal_youth_backend.myaccount.service;

import org.example.tnal_youth_backend.member.education.dto.request.MemberEducationRequest;
import org.example.tnal_youth_backend.member.education.dto.response.MemberEducationResponse;
import org.example.tnal_youth_backend.member.family.dto.request.MemberFamilyInfoRequest;
import org.example.tnal_youth_backend.member.family.dto.response.MemberFamilyInfoResponse;
import org.example.tnal_youth_backend.member.language.dto.request.MemberLanguageRequest;
import org.example.tnal_youth_backend.member.language.dto.response.MemberLanguageResponse;
import org.example.tnal_youth_backend.member.member.dto.response.MemberDetailResponse;
import org.example.tnal_youth_backend.member.participation.dto.response.MemberParticipationPageResponse;
import org.example.tnal_youth_backend.member.password.dto.response.MemberPasswordStatusResponse;
import org.example.tnal_youth_backend.member.personalinfo.dto.response.MemberPersonalInfoResponse;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.request.MemberPoliticalAffiliationRequest;
import org.example.tnal_youth_backend.member.politicalaffiliation.dto.response.MemberPoliticalAffiliationResponse;
import org.example.tnal_youth_backend.member.skill.dto.request.MemberSkillRequest;
import org.example.tnal_youth_backend.member.skill.dto.response.MemberSkillResponse;
import org.example.tnal_youth_backend.member.workhistory.dto.request.MemberWorkHistoryRequest;
import org.example.tnal_youth_backend.member.workhistory.dto.response.MemberWorkHistoryResponse;
import org.example.tnal_youth_backend.myaccount.dto.request.ChangeMyPasswordRequest;
import org.example.tnal_youth_backend.myaccount.dto.request.UpdateMyPersonalInfoRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MyAccountService {

    MemberDetailResponse getMyProfile();

    /*
     * Uploads and assigns the authenticated member's profile photo.
     * Delegates to MemberService so the same upload/storage logic used
     * by staff editing a member's photo is reused here.
     */
    MemberDetailResponse uploadMyProfilePhoto(
            MultipartFile file
    );



    MemberPersonalInfoResponse getMyPersonalInfo();

    MemberPersonalInfoResponse updateMyPersonalInfo(
            UpdateMyPersonalInfoRequest request
    );

    MemberPersonalInfoResponse uploadMyCv(
            MultipartFile file
    );

    MemberFamilyInfoResponse getMyFamilyInfo();

    MemberFamilyInfoResponse updateMyFamilyInfo(
            MemberFamilyInfoRequest request
    );

    List<MemberWorkHistoryResponse>
    getMyWorkHistory();

    MemberWorkHistoryResponse createMyWorkHistory(
            MemberWorkHistoryRequest request
    );

    MemberWorkHistoryResponse updateMyWorkHistory(
            Long workId,
            MemberWorkHistoryRequest request
    );

    void deleteMyWorkHistory(
            Long workId
    );

    List<MemberEducationResponse>
    getMyEducation();

    MemberEducationResponse createMyEducation(
            MemberEducationRequest request
    );

    MemberEducationResponse updateMyEducation(
            Long educationId,
            MemberEducationRequest request
    );

    void deleteMyEducation(
            Long educationId
    );

    MemberEducationResponse uploadMyEducationCertificate(
            Long educationId,
            MultipartFile file
    );

    List<MemberLanguageResponse>
    getMyLanguages();

    MemberLanguageResponse
    createMyLanguage(
            MemberLanguageRequest request
    );

    MemberLanguageResponse
    updateMyLanguage(
            Long languageId,
            MemberLanguageRequest request
    );

    void deleteMyLanguage(
            Long languageId
    );

    MemberLanguageResponse
    uploadMyLanguageCertificate(
            Long languageId,
            MultipartFile file
    );

    MemberLanguageResponse
    removeMyLanguageCertificate(
            Long languageId
    );

    List<MemberSkillResponse>
    getMySkills();

    MemberSkillResponse
    createMySkill(
            MemberSkillRequest request
    );

    MemberSkillResponse
    updateMySkill(
            Long skillId,
            MemberSkillRequest request
    );

    void deleteMySkill(
            Long skillId
    );

    MemberSkillResponse
    uploadMySkillCertificate(
            Long skillId,
            MultipartFile file
    );

    MemberSkillResponse
    removeMySkillCertificate(
            Long skillId
    );
    List<MemberPoliticalAffiliationResponse>
    getMyPoliticalAffiliations();

    MemberPoliticalAffiliationResponse
    getMyPoliticalAffiliation(
            Long affiliationId
    );

    MemberPoliticalAffiliationResponse
    createMyPoliticalAffiliation(
            MemberPoliticalAffiliationRequest request
    );

    MemberPoliticalAffiliationResponse
    updateMyPoliticalAffiliation(
            Long affiliationId,
            MemberPoliticalAffiliationRequest request
    );

    void deleteMyPoliticalAffiliation(
            Long affiliationId
    );

    MemberPasswordStatusResponse changeMyPassword(
            ChangeMyPasswordRequest request
    );

    MemberParticipationPageResponse
    getMyParticipations(
            int page,
            int size,
            String search,
            Short typeId,
            Short attendanceStatusId
    );
}
