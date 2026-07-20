package org.example.tnal_youth_backend.document.document.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record DocumentResponse(

        Long id,

        @JsonProperty("type_id")
        Short typeId,

        @JsonProperty("file_id")
        Long fileId,

        String title,

        String description,

        @JsonProperty("branch_id")
        Long branchId,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("activity_id")
        Long activityId,

        @JsonProperty("uploaded_by")
        Long uploadedById,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
}