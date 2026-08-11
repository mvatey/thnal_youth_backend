package org.example.tnal_youth_backend.document.document.dto.request;

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
                max = 500,
                message = "Document title must not exceed 500 characters"
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

    @AssertTrue(
            message = """
                    Exactly one owner is required: branch_id, \
                    member_id, or activity_id
                    """
    )
    public boolean isOwnerSelectionValid() {
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

        return ownerCount == 1;
    }
}