package org.example.tnal_youth_backend.donation.sponsor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "sponsors",
        indexes = {
                @Index(
                        name = "idx_sponsors_type_id",
                        columnList = "sponsor_type_id"
                ),
                @Index(
                        name = "idx_sponsors_active",
                        columnList = "is_active"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /*
     * sponsor_type_id -> sponsor_types.id
     */
    @Column(
            name = "sponsor_type_id",
            nullable = false
    )
    private Short sponsorTypeId;

    @Column(
            name = "name",
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            name = "phone",
            length = 30
    )
    private String phone;

    @Column(
            name = "email",
            columnDefinition = "citext"
    )
    private String email;

    @Column(
            name = "address",
            columnDefinition = "TEXT"
    )
    private String address;

    @Column(
            name = "note",
            columnDefinition = "TEXT"
    )
    private String note;

    @Builder.Default
    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive = true;

    /*
     * created_by -> users.id
     */
    @Column(name = "created_by")
    private Long createdById;

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

        if (isActive == null) {
            isActive = true;
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