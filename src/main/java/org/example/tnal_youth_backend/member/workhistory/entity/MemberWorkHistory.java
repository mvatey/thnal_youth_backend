package org.example.tnal_youth_backend.member.workhistory.entity;

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
        name = "member_work_history",
        indexes = {
                @Index(
                        name = "idx_member_work_history_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_member_work_history_sector_id",
                        columnList = "employment_sector_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberWorkHistory {

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
            name = "organization_name",
            nullable = false,
            length = 255
    )
    private String organizationName;

    @Column(
            name = "position_title",
            nullable = false,
            length = 255
    )
    private String positionTitle;

    @Column(
            name = "address",
            length = 255
    )
    private String address;

    /*
     * Database foreign key:
     * employment_sector_id → employment_sectors.id
     *
     * Kept as an ID because the employment-sector module
     * has not been built yet.
     */
    @Column(name = "employment_sector_id")
    private Short employmentSectorId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

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