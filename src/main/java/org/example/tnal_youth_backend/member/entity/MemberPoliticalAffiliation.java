package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "member_political_affiliations")
@Data
public class MemberPoliticalAffiliation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String partyNameEn;
    private String partyNameKh;
    private String roleEn;
    private String roleKh;
    private String countryEn;
    private String countryKh;
    private String appointmentCode;
    private String workplaceEn;
    private String workplaceKh;
    private LocalDate startDate;
    private LocalDate endDate;

    // NEW
    private String status;
}
