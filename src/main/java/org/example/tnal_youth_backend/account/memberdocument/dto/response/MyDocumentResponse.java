package org.example.tnal_youth_backend.account.memberdocument.dto.response;

import java.time.OffsetDateTime;

public record MyDocumentResponse(

        /*
         * Document information
         */
        Long documentId,

        String title,

        String description,

        /*
         * Document type
         */
        Long documentTypeId,

        String documentTypeCode,

        String documentTypeLabelKm,

        String documentTypeLabelEn,

        /*
         * File information
         */
        Long fileId,

        String filePath,

        String originalName,

        String mimeType,

        Long sizeBytes,

        OffsetDateTime fileCreatedAt,

        /*
         * Member ownership
         */
        Long memberId,

        String memberNo,

        String memberFullNameKm,

        String memberFullNameEn,

        /*
         * User who uploaded the document
         */
        Long uploadedByUserId,

        String uploadedByFullNameKm,

        String uploadedByFullNameEn,

        /*
         * Document timestamps
         */
        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}