package org.example.tnal_youth_backend.document.document.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record MemberDocumentTableItemResponse(

        Long id,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("member_name_km")
        String memberNameKm,

        @JsonProperty("member_name_en")
        String memberNameEn,

        String gender,

        @JsonProperty("document_type_id")
        Short documentTypeId,

        @JsonProperty("document_type_label_km")
        String documentTypeLabelKm,

        @JsonProperty("document_type_label_en")
        String documentTypeLabelEn,

        String title,

        String description,

        @JsonProperty("file_id")
        Long fileId,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt

) {
}