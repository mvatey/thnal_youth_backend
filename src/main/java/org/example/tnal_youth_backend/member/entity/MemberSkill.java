package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "member_skills")
@Data
public class MemberSkill {
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

    @Column(name = "listening_level")
    private String listeningLevel;

    @Column(name = "reading_level")
    private String readingLevel;

    @Column(name = "speaking_level")
    private String speakingLevel;

    @Column(name = "writing_level")
    private String writingLevel;

    @Column(name = "skill")
    private String skill;

    @Column(name = "cultural_level")
    private String culturalLevel;
}
