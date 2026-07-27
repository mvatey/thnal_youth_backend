package org.example.tnal_youth_backend.member.family.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.member.member.entity.Member;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "member_family",
        indexes = {
                @Index(
                        name = "idx_member_family_member_id",
                        columnList = "member_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberFamily {

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "relationship",
            nullable = false,
            length = 20
    )
    private FamilyRelationship relationship;

    @Column(
            name = "full_name_km",
            nullable = false,
            length = 255
    )
    private String fullNameKm;

    @Column(
            name = "full_name_en",
            length = 255
    )
    private String fullNameEn;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(
            name = "occupation",
            length = 255
    )
    private String occupation;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "life_status",
            length = 20
    )
    private FamilyLifeStatus lifeStatus;

    @Column(
            name = "address",
            length = 255
    )
    private String address;

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