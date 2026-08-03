package org.example.tnal_youth_backend.activity.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "title_km",
            nullable = false,
            length = 255
    )
    private String titleKm;

    @Column(
            name = "title_en",
            length = 255
    )
    private String titleEn;

    @Column(columnDefinition = "TEXT")
    private String description;

    /*
     * Activity lookup relationships
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private ActivityType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private ActivitySector sector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private ActivityStatus status;

    /*
     * These are currently ID fields because the Branch, Geography,
     * File and related module entities are not part of this phase.
     */

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean publicActivity = false;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Column(name = "province_id")
    private Short provinceId;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "commune_id")
    private Integer communeId;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "google_map_url", columnDefinition = "TEXT")
    private String googleMapUrl;

    @Column
    private Integer capacity;

    @Column(name = "cover_image_id")
    private Long coverImageId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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

        if (publicActivity == null) {
            publicActivity = false;
        }

        validateActivity();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
        validateActivity();
    }

    private void validateActivity() {
        if (titleKm == null || titleKm.isBlank()) {
            throw new IllegalStateException(
                    "Khmer activity title is required"
            );
        }

        if (startsAt == null || endsAt == null) {
            throw new IllegalStateException(
                    "Activity start and end times are required"
            );
        }

        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalStateException(
                    "Activity end time must be later than start time"
            );
        }

        if (capacity != null && capacity <= 0) {
            throw new IllegalStateException(
                    "Activity capacity must be greater than zero"
            );
        }

        if (communeId != null && districtId == null) {
            throw new IllegalStateException(
                    "A district is required when a commune is selected"
            );
        }
    }
}