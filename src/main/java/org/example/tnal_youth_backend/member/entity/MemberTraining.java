package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "member_training")
@Data
public class MemberTraining {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to Member
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "institution_en", nullable = false)
    private String institutionEn;

    @Column(name = "institution_kh")
    private String institutionKh;

    @Column(name = "province_en")
    private String provinceEn;

    @Column(name = "province_kh")
    private String provinceKh;

    @Column(name = "country_en")
    private String countryEn;

    @Column(name = "country_kh")
    private String countryKh;

    @Column(name = "degree_en")
    private String degreeEn;

    @Column(name = "degree_kh")
    private String degreeKh;

    @Column(name = "link")
    private String link; // តំណរភ្ជាប់

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "description_kh")
    private String descriptionKh;
}
