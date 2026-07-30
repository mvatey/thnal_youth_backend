package org.example.tnal_youth_backend.document.document.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DocumentRequest(

        @JsonProperty("type_id")
        @NotNull(message = "Document type ID is required")
        @Positive(message = "Document type ID must be positive")
        Short typeId,

        @JsonProperty("file_id")
        @NotNull(message = "File ID is required")
        @Positive(message = "File ID must be positive")
        Long fileId,

        @NotBlank(message = "Document title is required")
        @Size(
                max = 255,
                message = "Document title must not exceed 255 characters"
        )
        String title,

        @Size(
                max = 5000,
                message = "Description must not exceed 5000 characters"
        )
        String description,

        @JsonProperty("branch_id")
        @Positive(message = "Branch ID must be positive")
        Long branchId,

        @JsonProperty("member_id")
        @Positive(message = "Member ID must be positive")
        Long memberId,

        @JsonProperty("activity_id")
        @Positive(message = "Activity ID must be positive")
        Long activityId

) {

    /**
     * A document can belong to:
     *
     * 1. The whole organization: no owner ID is supplied.
     * 2. A branch: branch_id only.
     * 3. A member: member_id only.
     * 4. An activity: activity_id only.
     */
    @JsonIgnore
    @AssertTrue(
            message = "Only one owner may be provided: branch_id, member_id, or activity_id"
    )
    public boolean isOwnerSelectionValid() {
        return getOwnerCount() <= 1;
    }

    @JsonIgnore
    public boolean isOrganizationDocument() {
        return getOwnerCount() == 0;
    }

    @JsonIgnore
    public String normalizedTitle() {
        return title == null ? null : title.trim();
    }

    @JsonIgnore
    public String normalizedDescription() {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();

        return normalized.isEmpty() ? null : normalized;
    }

    private int getOwnerCount() {
        int ownerCount = 0;

        if (branchId != null) {
            ownerCount++;
        }

        if (memberId != null) {
            ownerCount++;
        }

        if (activityId != null) {
            ownerCount++;
        }

        return ownerCount;
    }
}