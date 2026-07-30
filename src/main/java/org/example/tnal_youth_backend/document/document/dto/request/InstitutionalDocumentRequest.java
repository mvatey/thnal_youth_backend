package org.example.tnal_youth_backend.document.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public final class InstitutionalDocumentRequest {

    private InstitutionalDocumentRequest() {
    }

    public record Create(
            @NotNull(message = "Branch ID is required")
            @Positive(message = "Branch ID must be positive")
            Long branchId,

            @NotBlank(message = "Document title is required")
            @Size(max = 255, message = "Document title must not exceed 255 characters")
            String title,

            @Size(max = 5000, message = "Description must not exceed 5000 characters")
            String description,

            @NotNull(message = "Document file is required")
            MultipartFile file
    ) {
        public String normalizedTitle() {
            return title == null ? null : title.trim();
        }

        public String normalizedDescription() {
            return normalizeDescription(description);
        }
    }

    public record Update(
            @NotNull(message = "Branch ID is required")
            @Positive(message = "Branch ID must be positive")
            Long branchId,

            @NotBlank(message = "Document title is required")
            @Size(max = 255, message = "Document title must not exceed 255 characters")
            String title,

            @Size(max = 5000, message = "Description must not exceed 5000 characters")
            String description,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate documentDate,

            MultipartFile file
    ) {
        public String normalizedTitle() {
            return title == null ? null : title.trim();
        }

        public String normalizedDescription() {
            return normalizeDescription(description);
        }
    }

    private static String normalizeDescription(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
