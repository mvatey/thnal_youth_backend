package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "member_languages")
@Data
public class MemberLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to Member
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "language_en", nullable = false)
    private String languageEn;

    @Column(name = "language_kh")
    private String languageKh;

    @Column(name = "proficiency_level")
    private String proficiencyLevel; // Beginner, Intermediate, Advanced, Fluent, Native
}
