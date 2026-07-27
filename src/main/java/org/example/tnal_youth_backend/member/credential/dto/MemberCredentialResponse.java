package org.example.tnal_youth_backend.member.credential.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberCredentialResponse {

    private Long id;

    private String title;

    private CredentialKindResponse credentialKind;

    private String credentialNo;

    private LocalDate issuedOn;

    private FileResponse file;

    private OffsetDateTime createdAt;

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
    public static class FileResponse {

        private Long id;

        private String url;

        private String originalName;

        private String mimeType;

        private Long sizeBytes;

        private Double sizeKb;

        private Double sizeMb;
    }
}