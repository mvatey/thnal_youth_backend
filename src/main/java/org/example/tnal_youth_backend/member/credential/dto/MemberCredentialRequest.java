package org.example.tnal_youth_backend.member.credential.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
                max = 30,
                message = "Credential kind must not exceed 30 characters"
        )
        private String credentialKind;

        @JsonProperty("credentialNo")
        @JsonAlias("credential_no")
        @NotBlank(message = "Credential number is required")
        @Size(
                max = 100,
                message = "Credential number must not exceed 100 characters"
        )
        private String credentialNo;

        @JsonProperty("activityId")
        @JsonAlias("activity_id")
        private Long activityId;

        @JsonProperty("issuedOn")
        @JsonAlias("issued_on")
        @NotNull(message = "Issued date is required")
        private LocalDate issuedOn;

        @JsonProperty("fileId")
        @JsonAlias("file_id")
        private Long fileId;
}