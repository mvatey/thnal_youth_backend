package org.example.tnal_youth_backend.member.position.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "positions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "positions_code_key",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

    @Column(
            name = "label_km",
            nullable = false,
            length = 100
    )
    private String labelKm;

    @Column(
            name = "label_en",
            length = 100
    )
    private String labelEn;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder;

    /**
     * The system role a member holding this position is assigned when
     * created — see MemberServiceImpl#createMember. NULL means this
     * position has no auto-assigned role (the create flow falls back to
     * MEMBER). Restricted at the database level (chk_positions_mapped_role,
     * V339) to BRANCH_LEADER, SECRETARY, or MEMBER — ADMIN/VIEWER are never
     * appropriate as a position-derived role for a member.
     */
    @Column(
            name = "mapped_role",
            length = 30
    )
    private String mappedRole;

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

        if (isActive == null) {
            isActive = true;
        }

        if (sortOrder == null) {
            sortOrder = 0;
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
        updatedAt = OffsetDateTime.now();
    }
}
