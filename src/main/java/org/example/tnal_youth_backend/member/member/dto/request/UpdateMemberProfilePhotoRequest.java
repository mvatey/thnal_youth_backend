package org.example.tnal_youth_backend.member.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberProfilePhotoRequest(

        @NotNull
        @JsonProperty("profile_photo_id")
        Long profilePhotoId
) {
}