package org.example.tnal_youth_backend.member.politicalaffiliation.entity;

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
        name = "member_political_affiliations",
        indexes = {
                @Index(
                        name = "idx_member_political_affiliation_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_member_political_affiliation_party_id",
                        columnList = "party_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPoliticalAffiliation {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
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
            name = "party_id",
            nullable = false
    )
    private Short partyId;

    @Column(
            name = "country",
            length = 100
    )
    private String country;

    @Column(
            name = "location",
            length = 255
    )
    private String location;

    @Column(
            name = "position_title",
            length = 255
    )
    private String positionTitle;

    @Column(
            name = "card_no",
            length = 100
    )
    private String cardNo;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(
            name = "is_current",
            nullable = false
    )
    private Boolean isCurrent;

    @Column(
            name = "note",
            columnDefinition = "text"
    )
    private String note;

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
        OffsetDateTime now =
                OffsetDateTime.now();

        if (isCurrent == null) {
            isCurrent = false;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt =
                OffsetDateTime.now();
    }
}