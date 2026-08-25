package org.example.tnal_youth_backend.organization.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationProfileRequest(
        @JsonProperty("name_km")
        @NotBlank(message = "Organization Khmer name is required")
        @Size(max = 255, message = "Organization Khmer name must not exceed 255 characters")
        String nameKm,

        @JsonProperty("name_en")
        @Size(max = 255, message = "Organization English name must not exceed 255 characters")
        String nameEn,

        @JsonProperty("tagline_km")
        @Size(max = 255, message = "Organization Khmer subtitle must not exceed 255 characters")
        String taglineKm,

        @JsonProperty("tagline_en")
        @Size(max = 255, message = "Organization English subtitle must not exceed 255 characters")
        String taglineEn,

        @JsonProperty("hero_headline_km")
        @Size(max = 255, message = "Khmer hero headline must not exceed 255 characters")
        String heroHeadlineKm,

        @JsonProperty("hero_headline_en")
        @Size(max = 255, message = "English hero headline must not exceed 255 characters")
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
        String youtubeUrl
) {
}
