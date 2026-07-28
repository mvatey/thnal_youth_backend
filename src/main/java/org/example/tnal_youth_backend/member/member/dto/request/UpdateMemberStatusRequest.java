package org.example.tnal_youth_backend.member.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberStatusRequest(

        @JsonProperty("status_id")
        @NotNull(message = "Member status ID is required")
        Short statusId
) {
}