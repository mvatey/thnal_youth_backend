package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "member_political_affiliations")
@Data
public class MemberPoliticalAffiliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to Member
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "party_name_en", nullable = false)
    private String partyNameEn;

    @Column(name = "party_name_kh")
    private String partyNameKh;

    @Column(name = "role_en")
    private String roleEn;

    @Column(name = "role_kh")
    private String roleKh;

    @Column(name = "country_en")
    private String countryEn;

    @Column(name = "country_kh")
    private String countryKh;

    @Column(name = "appointment_code")
    private String appointmentCode;

    @Column(name = "workplace_en")
    private String workplaceEn;

    @Column(name = "workplace_kh")
    private String workplaceKh;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
