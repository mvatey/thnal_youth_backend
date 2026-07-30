package org.example.tnal_youth_backend.document.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentListItemResponse(

        Long id,

        String title,

        String description,

        String typeCode,

        String typeName,

        Long fileId,

        String originalFileName,

        String mimeType,

        String fileExtension,

        Long sizeBytes,

        Double sizeMb,

        String contentUrl,

        /*
         * Shared ownership fields.
         *
         * These remain unchanged so Institutional Documents continue
         * using the existing response structure.
         */
        String ownerType,

        Long ownerId,

        String ownerName,

        /*
         * Institutional branch fields.
         *
         * These remain unchanged.
         */
        Long branchId,

        String branchName,

        /*
         * Member-specific fields.
         *
         * These fields are populated only when ownerType is MEMBER.
         */
        Long memberId,

        String memberName,

        String genderCode,

        String genderName,

        Long memberBranchId,

        String memberBranchName,

        LocalDate documentDate
) {
}