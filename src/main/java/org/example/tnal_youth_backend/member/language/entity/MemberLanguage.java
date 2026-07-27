package org.example.tnal_youth_backend.member.language.entity;

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
        name = "member_languages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_member_language_name",
                        columnNames = {
                                "member_id",
                                "language_name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_member_languages_member_id",
                        columnList = "member_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLanguage {

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
            name = "language_name",
            nullable = false,
            length = 100
    )
    private String languageName;

    /*
     * Foreign keys:
     *
     * listening_level_id → proficiency_levels.id
     * speaking_level_id  → proficiency_levels.id
     * reading_level_id   → proficiency_levels.id
     * writing_level_id   → proficiency_levels.id
     */

    @Column(name = "listening_level_id")
    private Short listeningLevelId;

    @Column(name = "speaking_level_id")
    private Short speakingLevelId;

    @Column(name = "reading_level_id")
    private Short readingLevelId;

    @Column(name = "writing_level_id")
    private Short writingLevelId;

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