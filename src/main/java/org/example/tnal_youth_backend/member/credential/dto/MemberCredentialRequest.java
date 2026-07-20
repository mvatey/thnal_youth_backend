package org.example.tnal_youth_backend.member.credential.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCredentialRequest {

        @NotBlank(message = "Title is required")
        @Size(
                max = 255,
                message = "Title must not exceed 255 characters"
        )
        private String title;

        @JsonProperty("credentialKind")
        @JsonAlias("credential_kind")
        @NotBlank(message = "Credential kind is required")
        @Size(
                max = 100,
                message = "Credential kind must not exceed 100 characters"
        )
        private String credentialKind;

        @JsonProperty("credentialNo")
        @JsonAlias("credential_no")
        @Size(
                max = 150,
                message = "Credential number must not exceed 150 characters"
        )
        private String credentialNo;

        @JsonProperty("issuedOn")
        @JsonAlias("issued_on")
        private LocalDate issuedOn;

        @JsonProperty("issuedById")
        @JsonAlias({
                "issued_by_id",
                "issued_by"
        })
        private Long issuedById;

        @JsonProperty("fileId")
        @JsonAlias("file_id")
        private Long fileId;
}