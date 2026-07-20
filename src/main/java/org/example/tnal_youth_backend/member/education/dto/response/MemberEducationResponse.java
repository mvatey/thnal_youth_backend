package org.example.tnal_youth_backend.member.education.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MemberEducationResponse(

        Long id,

        @JsonProperty("member_id")
        Long memberId,

        @JsonProperty("school_name")
        String schoolName,

        @JsonProperty("education_level_id")
        Short educationLevelId,

        @JsonProperty("field_of_study")
        String fieldOfStudy,

        @JsonProperty("country_code")
        String countryCode,

        @JsonProperty("country_name")
        String countryName,

        @JsonProperty("province_id")
        Short provinceId,

        @JsonProperty("province_name")
        String provinceName,

        @JsonProperty("certificate_file")
        FileResponse certificateFile,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {

    public record FileResponse(
            Long id,

            @JsonProperty("file_path")
            String filePath,

            @JsonProperty("original_name")
            String originalName,

            @JsonProperty("mime_type")
            String mimeType,

            @JsonProperty("size_bytes")
            Long sizeBytes
    ) {
    }
}