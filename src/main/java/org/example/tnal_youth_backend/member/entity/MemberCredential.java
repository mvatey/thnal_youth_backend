package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "member_credentials")
@Data
public class MemberCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to Member
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "credential_name_en", nullable = false)
    private String credentialNameEn;

    @Column(name = "credential_name_kh")
    private String credentialNameKh;

    @Column(name = "issued_by_en")
    private String issuedByEn;

    @Column(name = "issued_by_kh")
    private String issuedByKh;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "description_kh")
    private String descriptionKh;
}
