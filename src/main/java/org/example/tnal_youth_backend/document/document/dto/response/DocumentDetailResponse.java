package org.example.tnal_youth_backend.document.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentDetailResponse(

        Long id,

        Short typeId,

        String title,

        String description,

        String typeCode,

        String typeName,

        Long fileId,

        String originalFileName,

        String mimeType,

        Long sizeBytes,

        Double sizeMb,

        String contentUrl,

        /*
         * Existing activity ownership field.
         */
        Long activityId,

        /*
         * Existing Institutional branch fields.
         */
        Long branchId,

        String branchName,

        LocalDate documentDate,

        /*
         * Shared ownership fields.
         */
        String ownerType,

        Long ownerId,

        String ownerName,

        /*
         * Member-specific fields.
         *
         * These are populated only for member-owned documents.
         */
        Long memberId,

        String memberName,

        String genderCode,

        String genderName,

        Long memberBranchId,

        String memberBranchName,

        /*
         * Upload information.
         */
        Long uploadedById,

        String uploadedByName,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}