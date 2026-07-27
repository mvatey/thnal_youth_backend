package org.example.tnal_youth_backend.document.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentResponse(

        Long id,

        String title,

        String description,

        DocumentTypeResponse type,

        FileResponse file,

        BranchResponse branch,

        MemberResponse member,

        ActivityResponse activity,

        UploadedByResponse uploadedBy,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt

) {

        public record DocumentTypeResponse(

                Short id,

                String code,

                @JsonProperty("label_km")
                String labelKm,

                @JsonProperty("label_en")
                String labelEn

        ) {
        }

        public record FileResponse(

                Long id,

                String url,

                String originalName,

                String mimeType,

                Long sizeBytes,

                Double sizeKb,

                Double sizeMb

        ) {
        }

        public record BranchResponse(

                Long id,

                @JsonProperty("name_km")
                String nameKm,

                @JsonProperty("name_en")
                String nameEn

        ) {
        }

        public record MemberResponse(

                Long id,

                @JsonProperty("member_no")
                String memberNo,

                @JsonProperty("full_name_km")
                String fullNameKm,

                @JsonProperty("full_name_en")
                String fullNameEn

        ) {
        }

        public record ActivityResponse(

                Long id,

                @JsonProperty("title_km")
                String titleKm,

                @JsonProperty("title_en")
                String titleEn

        ) {
        }

        public record UploadedByResponse(

                Long id,

                @JsonProperty("full_name_km")
                String fullNameKm,

                @JsonProperty("full_name_en")
                String fullNameEn

        ) {
        }
}