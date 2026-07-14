package org.example.tnal_youth_backend.member.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "member_skills")
@Data
public class MemberSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String skill;
    private String culturalLevel;

    // Deprecated fields kept for compatibility
    private String languageEn;
    private String languageKh;
    private String listeningLevel;
    private String readingLevel;
    private String speakingLevel;
    private String writingLevel;
}
