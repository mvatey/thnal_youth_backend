package org.example.tnal_youth_backend.organization.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.tnal_youth_backend.organization.entity.OrganizationProfile;

import java.time.OffsetDateTime;

public record OrganizationProfileResponse(
        Short id,

        @JsonProperty("name_km")
        String nameKm,

        @JsonProperty("name_en")
        String nameEn,

        @JsonProperty("tagline_km")
        String taglineKm,

        @JsonProperty("tagline_en")
        String taglineEn,

        @JsonProperty("hero_headline_km")
        String heroHeadlineKm,

        @JsonProperty("hero_headline_en")
        String heroHeadlineEn,

        @JsonProperty("hero_description_km")
        String heroDescriptionKm,

        @JsonProperty("hero_description_en")
        String heroDescriptionEn,

        @JsonProperty("about_km")
        String aboutKm,

        @JsonProperty("about_en")
        String aboutEn,

        String address,
        String phone,
        String email,
        String website,

        @JsonProperty("facebook_url")
        String facebookUrl,

        @JsonProperty("telegram_url")
        String telegramUrl,

        @JsonProperty("youtube_url")
        String youtubeUrl,

        @JsonProperty("logo_file_id")
        Long logoFileId,

        @JsonProperty("logo_url")
        String logoUrl,

        @JsonProperty("cover_file_id")
        Long coverFileId,

        @JsonProperty("cover_url")
        String coverUrl,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
    public static OrganizationProfileResponse from(OrganizationProfile profile) {
        Long logoFileId = profile.getLogoFile() == null ? null : profile.getLogoFile().getId();
        Long coverFileId = profile.getCoverFile() == null ? null : profile.getCoverFile().getId();

        return new OrganizationProfileResponse(
                profile.getId(),
                profile.getNameKm(),
                profile.getNameEn(),
                profile.getTaglineKm(),
                profile.getTaglineEn(),
                profile.getHeroHeadlineKm(),
                profile.getHeroHeadlineEn(),
                profile.getHeroDescriptionKm(),
                profile.getHeroDescriptionEn(),
                profile.getAboutKm(),
                profile.getAboutEn(),
                profile.getAddress(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getWebsite(),
                profile.getFacebookUrl(),
                profile.getTelegramUrl(),
                profile.getYoutubeUrl(),
                logoFileId,
                logoFileId == null ? null : "/api/files/" + logoFileId + "/content",
                coverFileId,
                coverFileId == null ? null : "/api/files/" + coverFileId + "/content",
                profile.getUpdatedAt()
        );
    }
}
