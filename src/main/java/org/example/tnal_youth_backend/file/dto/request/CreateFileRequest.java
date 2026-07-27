package org.example.tnal_youth_backend.file.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateFileRequest(

        @NotBlank(message = "File path is required")
        String filePath,

        @NotBlank(message = "Original file name is required")
        @Size(
                max = 255,
                message = "Original file name must not exceed 255 characters"
        )
        String originalName,

        @NotBlank(message = "MIME type is required")
        @Size(
                max = 100,
                message = "MIME type must not exceed 100 characters"
        )
        String mimeType,

        @NotNull(message = "File size is required")
        @Positive(message = "File size must be greater than zero")
        Long sizeBytes,

        Long uploadedById
) {
}