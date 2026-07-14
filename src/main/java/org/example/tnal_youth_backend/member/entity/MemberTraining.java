package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "member_training")
@Data
public class MemberTraining {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String institutionEn;
    private String institutionKh;
    private String provinceEn;
    private String provinceKh;
    private String countryEn;
    private String countryKh;
    private String degreeEn;
    private String degreeKh;
    private String link;
    private LocalDate startDate;
    private LocalDate endDate;
    private String descriptionEn;
    private String descriptionKh;

    // NEW
    private String majorEn;
    private String majorKh;
}
