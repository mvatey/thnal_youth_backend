package org.example.tnal_youth_backend.member.skill.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.member.member.entity.Member;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "member_skills",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_member_skill_name",
                        columnNames = {
                                "member_id",
                                "skill_name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_member_skills_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_member_skills_proficiency_level_id",
                        columnList = "proficiency_level_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Column(
            name = "skill_name",
            nullable = false,
            length = 255
    )
    private String skillName;

    /*
     * Database foreign key:
     * member_skills.proficiency_level_id
     * → proficiency_levels.id
     */
    @Column(name = "proficiency_level_id")
    private Short proficiencyLevelId;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}