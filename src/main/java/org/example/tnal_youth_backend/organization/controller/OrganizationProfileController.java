package org.example.tnal_youth_backend.organization.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.service.FileService;
import org.example.tnal_youth_backend.organization.dto.request.UpdateOrganizationProfileRequest;
import org.example.tnal_youth_backend.organization.dto.response.OrganizationProfileResponse;
import org.example.tnal_youth_backend.organization.entity.OrganizationProfile;
import org.example.tnal_youth_backend.organization.repository.OrganizationProfileRepository;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/organization-profile")
@RequiredArgsConstructor
public class OrganizationProfileController {

    private static final short SINGLE_PROFILE_ID = 1;

    private final OrganizationProfileRepository repository;
    private final FileService fileService;

    @GetMapping
    public OrganizationProfileResponse getProfile() {
        return OrganizationProfileResponse.from(loadProfile());
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationProfileResponse updateProfile(
            @Valid @RequestBody UpdateOrganizationProfileRequest request
    ) {
        OrganizationProfile profile = loadProfile();
        profile.setNameKm(cleanRequired(request.nameKm()));
        profile.setNameEn(clean(request.nameEn()));
        profile.setTaglineKm(clean(request.taglineKm()));
        profile.setTaglineEn(clean(request.taglineEn()));
        profile.setHeroHeadlineKm(clean(request.heroHeadlineKm()));
        profile.setHeroHeadlineEn(clean(request.heroHeadlineEn()));
        profile.setHeroDescriptionKm(clean(request.heroDescriptionKm()));
        profile.setHeroDescriptionEn(clean(request.heroDescriptionEn()));
        profile.setAboutKm(clean(request.aboutKm()));
        profile.setAboutEn(clean(request.aboutEn()));
        profile.setAddress(clean(request.address()));
        profile.setPhone(clean(request.phone()));
        profile.setEmail(clean(request.email()));
        profile.setWebsite(clean(request.website()));
        profile.setFacebookUrl(clean(request.facebookUrl()));
        profile.setTelegramUrl(clean(request.telegramUrl()));
        profile.setYoutubeUrl(clean(request.youtubeUrl()));
        profile.setUpdatedBy(SecurityUtils.getCurrentUserId());

        return OrganizationProfileResponse.from(repository.save(profile));
    }

    @PostMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationProfileResponse uploadLogo(
            @RequestParam("file") MultipartFile file
    ) {
        OrganizationProfile profile = loadProfile();
        FileEntity uploaded = fileService.uploadImage(file, SecurityUtils.getCurrentUserId());
        profile.setLogoFile(uploaded);
        profile.setUpdatedBy(SecurityUtils.getCurrentUserId());

        return OrganizationProfileResponse.from(repository.save(profile));
    }

    private OrganizationProfile loadProfile() {
        return repository.findById(SINGLE_PROFILE_ID)
                .orElseGet(() -> {
                    OrganizationProfile profile = new OrganizationProfile();
                    profile.setId(SINGLE_PROFILE_ID);
                    profile.setNameKm("សមាគមថ្នាលយុវជនកម្ពុជា");
                    profile.setNameEn("Cambodian Youth Nursery Association");
                    profile.setTaglineKm("ការគ្រប់គ្រងប្រព័ន្ធយុវជន");
                    profile.setTaglineEn("Youth management system");
                    profile.setHeroHeadlineKm("សមាជិក · សកម្មភាព · វិភាគទាន");
                    profile.setHeroHeadlineEn("Members · Activities · Donations");
                    profile.setHeroDescriptionKm("គ្រប់គ្រងទិន្នន័យសមាជិក ការបង់វិភាគទាន និងសកម្មភាពទាំងនៅទីនេះដោយពួកគេ");
                    profile.setHeroDescriptionEn("Manage member data, donations, and activities in one place.");
                    return repository.save(profile);
                });
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String cleanRequired(String value) {
        String cleaned = clean(value);
        return cleaned == null ? "សមាគមថ្នាលយុវជនកម្ពុជា" : cleaned;
    }
}
