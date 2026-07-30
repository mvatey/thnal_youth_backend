package org.example.tnal_youth_backend.document.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public final class MemberDocumentRequest {

    private MemberDocumentRequest() {
    }

    /**
     * Creates a member-owned document.
     *
     * This request supports both Create UI flows:
     *
     * 1. A generated member card, letter, or certificate image.
     * 2. A personal document uploaded directly by the user.
     *
     * The selected branch is not submitted because it is used by the
     * frontend only to filter the available members. The actual document
     * owner is the selected member.
     */
    public record Create(

            @NotNull(message = "Member ID is required")
            @Positive(message = "Member ID must be positive")
            Long memberId,

            @NotNull(message = "Document type ID is required")
            @Positive(message = "Document type ID must be positive")
            Short typeId,

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

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate documentDate,

            @NotNull(message = "Document file is required")
            MultipartFile file
    ) {

        /**
         * Returns a trimmed title before persistence.
         */
        public String normalizedTitle() {
            return title == null
                    ? null
                    : title.trim();
        }

        /**
         * Converts an empty description into null.
         */
        public String normalizedDescription() {
            return normalizeDescription(description);
        }

        /**
         * Uses today's date when the frontend does not submit a document date.
         */
        public LocalDate effectiveDocumentDate() {
            return documentDate == null
                    ? LocalDate.now()
                    : documentDate;
        }

        /**
         * Indicates whether the uploaded file actually contains content.
         */
        public boolean hasFile() {
            return file != null && !file.isEmpty();
        }
    }

    private static String normalizeDescription(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}