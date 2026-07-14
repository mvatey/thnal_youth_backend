package org.example.tnal_youth_backend.member.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberCredentialDto {
    private Long memberId;
    private String credentialNameEn;
    private String credentialNameKh;
    private String issuedByEn;
    private String issuedByKh;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String certificateNumber;
    private String descriptionEn;
    private String descriptionKh;

    // NEW: for uploaded certificate/document
    private String fileUrl;
}

