package org.example.tnal_youth_backend.file.dto.response;

import java.time.OffsetDateTime;

public record FileResponse(
        Long id,
        String filePath,
        String originalName,
        String mimeType,
        Long sizeBytes,
        Double sizeKb,
        Double sizeMb,
        Long uploadedById,
        OffsetDateTime createdAt
) {
}