package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "member_credentials")
@Data
public class MemberCredential {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String credentialNameEn;
    private String credentialNameKh;
    private String issuedByEn;
    private String issuedByKh;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String certificateNumber;
    private String descriptionEn;
    private String descriptionKh;

    // NEW
    private String fileUrl;
}
