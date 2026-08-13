package org.example.tnal_youth_backend.myaccount.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.example.tnal_youth_backend.myaccount.service.MyAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/my-account")
@RequiredArgsConstructor
@Tag(
        name = "C. My Account",
        description =
                "Access information belonging to the authenticated account"
)
@PreAuthorize("isAuthenticated() and !hasRole('ADMIN')")
public class MyAccountController {

    private final MyAccountService
            myAccountService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberDetailResponse>
    getMyProfile() {
        return ResponseEntity.ok(
                myAccountService.getMyProfile()
        );
    }

    @GetMapping("/personal-info")
    public ResponseEntity<MemberPersonalInfoResponse>
    getMyPersonalInfo() {
        return ResponseEntity.ok(
                myAccountService
                        .getMyPersonalInfo()
        );
    }

    @PutMapping("/personal-info")
    public ResponseEntity<MemberPersonalInfoResponse>
    updateMyPersonalInfo(
            @Valid
            @RequestBody
            UpdateMyPersonalInfoRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .updateMyPersonalInfo(
                                request
                        )
        );
    }

    @PutMapping(
            value = "/personal-info/cv",
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MemberPersonalInfoResponse>
    uploadMyCv(
            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .uploadMyCv(
                                file
                        )
        );
    }



    @GetMapping("/family")
    public ResponseEntity<MemberFamilyInfoResponse>
    getMyFamilyInfo() {
        return ResponseEntity.ok(
                myAccountService.getMyFamilyInfo()
        );
    }

    @PutMapping("/family")
    public ResponseEntity<MemberFamilyInfoResponse>
    updateMyFamilyInfo(
            @Valid
            @RequestBody
            MemberFamilyInfoRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService.updateMyFamilyInfo(
                        request
                )
        );
    }

    @GetMapping("/work-history")
    public ResponseEntity<List<MemberWorkHistoryResponse>>
    getMyWorkHistory() {
        return ResponseEntity.ok(
                myAccountService.getMyWorkHistory()
        );
    }

    @PostMapping("/work-history")
    public ResponseEntity<MemberWorkHistoryResponse>
    createMyWorkHistory(
            @Valid
            @RequestBody
            MemberWorkHistoryRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        myAccountService
                                .createMyWorkHistory(request)
                );
    }

    @PutMapping("/work-history/{workId}")
    public ResponseEntity<MemberWorkHistoryResponse>
    updateMyWorkHistory(
            @PathVariable Long workId,

            @Valid
            @RequestBody
            MemberWorkHistoryRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService.updateMyWorkHistory(
                        workId,
                        request
                )
        );
    }

    @DeleteMapping("/work-history/{workId}")
    public ResponseEntity<Void>
    deleteMyWorkHistory(
            @PathVariable Long workId
    ) {
        myAccountService.deleteMyWorkHistory(
                workId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/education")
    public ResponseEntity<List<MemberEducationResponse>>
    getMyEducation() {
        return ResponseEntity.ok(
                myAccountService.getMyEducation()
        );
    }

    @PostMapping("/education")
    public ResponseEntity<MemberEducationResponse>
    createMyEducation(
            @Valid
            @RequestBody
            MemberEducationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        myAccountService
                                .createMyEducation(
                                        request
                                )
                );
    }

    @PutMapping("/education/{educationId}")
    public ResponseEntity<MemberEducationResponse>
    updateMyEducation(
            @PathVariable Long educationId,

            @Valid
            @RequestBody
            MemberEducationRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .updateMyEducation(
                                educationId,
                                request
                        )
        );
    }

    @DeleteMapping("/education/{educationId}")
    public ResponseEntity<Void>
    deleteMyEducation(
            @PathVariable Long educationId
    ) {
        myAccountService.deleteMyEducation(
                educationId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PutMapping(
            value =
                    "/education/{educationId}/certificate",
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MemberEducationResponse>
    uploadMyEducationCertificate(
            @PathVariable Long educationId,

            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .uploadMyEducationCertificate(
                                educationId,
                                file
                        )
        );
    }

    /*
     * ==========================================================
     * LANGUAGE
     * ==========================================================
     */

    @GetMapping("/languages")
    public ResponseEntity<List<MemberLanguageResponse>>
    getMyLanguages() {
        return ResponseEntity.ok(
                myAccountService
                        .getMyLanguages()
        );
    }

    @PostMapping("/languages")
    public ResponseEntity<MemberLanguageResponse>
    createMyLanguage(
            @Valid
            @RequestBody
            MemberLanguageRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        myAccountService
                                .createMyLanguage(
                                        request
                                )
                );
    }

    @PutMapping("/languages/{languageId}")
    public ResponseEntity<MemberLanguageResponse>
    updateMyLanguage(
            @PathVariable Long languageId,

            @Valid
            @RequestBody
            MemberLanguageRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .updateMyLanguage(
                                languageId,
                                request
                        )
        );
    }

    @DeleteMapping("/languages/{languageId}")
    public ResponseEntity<Void>
    deleteMyLanguage(
            @PathVariable Long languageId
    ) {
        myAccountService
                .deleteMyLanguage(
                        languageId
                );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PutMapping(
            value =
                    "/languages/{languageId}/certificate",
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MemberLanguageResponse>
    uploadMyLanguageCertificate(
            @PathVariable Long languageId,

            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .uploadMyLanguageCertificate(
                                languageId,
                                file
                        )
        );
    }

    @DeleteMapping(
            "/languages/{languageId}/certificate"
    )
    public ResponseEntity<MemberLanguageResponse>
    removeMyLanguageCertificate(
            @PathVariable Long languageId
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .removeMyLanguageCertificate(
                                languageId
                        )
        );
    }

    /*
     * ==========================================================
     * SKILLS
     * ==========================================================
     */

    @GetMapping("/skills")
    public ResponseEntity<List<MemberSkillResponse>>
    getMySkills() {
        return ResponseEntity.ok(
                myAccountService
                        .getMySkills()
        );
    }

    @PostMapping("/skills")
    public ResponseEntity<MemberSkillResponse>
    createMySkill(
            @Valid
            @RequestBody
            MemberSkillRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        myAccountService
                                .createMySkill(
                                        request
                                )
                );
    }

    @PutMapping("/skills/{skillId}")
    public ResponseEntity<MemberSkillResponse>
    updateMySkill(
            @PathVariable Long skillId,

            @Valid
            @RequestBody
            MemberSkillRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .updateMySkill(
                                skillId,
                                request
                        )
        );
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<Void>
    deleteMySkill(
            @PathVariable Long skillId
    ) {
        myAccountService
                .deleteMySkill(
                        skillId
                );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PutMapping(
            value =
                    "/skills/{skillId}/certificate",
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MemberSkillResponse>
    uploadMySkillCertificate(
            @PathVariable Long skillId,

            @RequestPart("file")
            MultipartFile file
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .uploadMySkillCertificate(
                                skillId,
                                file
                        )
        );
    }

    @DeleteMapping(
            "/skills/{skillId}/certificate"
    )
    public ResponseEntity<MemberSkillResponse>
    removeMySkillCertificate(
            @PathVariable Long skillId
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .removeMySkillCertificate(
                                skillId
                        )
        );
    }

    /*
     * ==========================================================
     * POLITICAL AFFILIATIONS
     * ==========================================================
     */

    @GetMapping("/political-affiliations")
    public ResponseEntity<
            List<MemberPoliticalAffiliationResponse>
            >
    getMyPoliticalAffiliations() {
        return ResponseEntity.ok(
                myAccountService
                        .getMyPoliticalAffiliations()
        );
    }

    @GetMapping(
            "/political-affiliations/{affiliationId}"
    )
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            >
    getMyPoliticalAffiliation(
            @PathVariable Long affiliationId
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .getMyPoliticalAffiliation(
                                affiliationId
                        )
        );
    }

    @PostMapping("/political-affiliations")
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            >
    createMyPoliticalAffiliation(
            @Valid
            @RequestBody
            MemberPoliticalAffiliationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        myAccountService
                                .createMyPoliticalAffiliation(
                                        request
                                )
                );
    }

    @PutMapping(
            "/political-affiliations/{affiliationId}"
    )
    public ResponseEntity<
            MemberPoliticalAffiliationResponse
            >
    updateMyPoliticalAffiliation(
            @PathVariable Long affiliationId,

            @Valid
            @RequestBody
            MemberPoliticalAffiliationRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .updateMyPoliticalAffiliation(
                                affiliationId,
                                request
                        )
        );
    }

    @DeleteMapping(
            "/political-affiliations/{affiliationId}"
    )
    public ResponseEntity<Void>
    deleteMyPoliticalAffiliation(
            @PathVariable Long affiliationId
    ) {
        myAccountService
                .deleteMyPoliticalAffiliation(
                        affiliationId
                );

        return ResponseEntity
                .noContent()
                .build();
    }

    /*
     * ==========================================================
     * PASSWORD
     * ==========================================================
     */

    @PatchMapping("/password")
    public ResponseEntity<MemberPasswordStatusResponse>
    changeMyPassword(
            @Valid
            @RequestBody
            ChangeMyPasswordRequest request
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .changeMyPassword(
                                request
                        )
        );
    }

    /*
     * ==========================================================
     * PARTICIPATION
     * ==========================================================
     */

    @GetMapping("/participations")
    public ResponseEntity<MemberParticipationPageResponse>
    getMyParticipations(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Short typeId,

            @RequestParam(required = false)
            Short attendanceStatusId
    ) {
        return ResponseEntity.ok(
                myAccountService
                        .getMyParticipations(
                                page,
                                size,
                                search,
                                typeId,
                                attendanceStatusId
                        )
        );
    }
}
