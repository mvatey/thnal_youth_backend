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
        Long activityId,

        /**
         * Skips the automatic "new document issued" notification. Used only
         * by flows that already send a more specific notification of their
         * own right after this document is created (e.g. an activity
         * certificate, which triggers a "certificate ready" notification
         * when its credential is linked) -- without this, the recipient
         * would get two notifications for the same certificate.
         */
        @JsonProperty("suppress_notification")
        Boolean suppressNotification,

        /**
         * Set only when this member-owned document is a personal activity
         * certificate. Unlike {@code activityId} (an exclusive "owner"
         * field — see isOwnerSelectionValid()), this is pure extra context:
         * it tells the access check to authorize based on that activity's
         * HOST branch instead of the recipient member's own branch, the
         * same carve-out already applied to the credential this document
         * gets linked to right after. Without it, the activity's own
         * organizing branch could never certify a co-hosting branch's
         * member, since that member's own branch is outside the
         * organizer's normal access scope.
         */
        @JsonProperty("certificate_activity_id")
        @Positive(message = "Certificate activity ID must be positive")
        Long certificateActivityId

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
