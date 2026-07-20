package org.example.tnal_youth_backend.member.credential.dto;

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
public class MemberCredentialResponse {

    private Long id;
    private Long memberId;
    private String title;
    private String credentialKind;
    private String credentialNo;
    private LocalDate issuedOn;
    private Long issuedById;
    private Long fileId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}