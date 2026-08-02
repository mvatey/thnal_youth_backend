package org.example.tnal_youth_backend.member.credential.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.member.credential.entity.CredentialStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberCredentialResponse {

    private Long id;

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("activity_id")
    private Long activityId;

    private String title;

    @JsonProperty("credential_kind")
    private CredentialKindResponse credentialKind;

    @JsonProperty("credential_no")
    private String credentialNo;

    @JsonProperty("issued_on")
    private LocalDate issuedOn;

    private CredentialStatus status;

    @JsonProperty("issued_by")
    private IssuedByResponse issuedBy;

    private FileResponse file;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialKindResponse {

        private String code;

        @JsonProperty("label_km")
        private String labelKm;

        @JsonProperty("label_en")
        private String labelEn;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssuedByResponse {

        private Long id;

        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileResponse {

        private Long id;

        private String url;

        @JsonProperty("original_name")
        private String originalName;

        @JsonProperty("mime_type")
        private String mimeType;

        @JsonProperty("size_bytes")
        private Long sizeBytes;

        @JsonProperty("size_kb")
        private Double sizeKb;

        @JsonProperty("size_mb")
        private Double sizeMb;
    }
}