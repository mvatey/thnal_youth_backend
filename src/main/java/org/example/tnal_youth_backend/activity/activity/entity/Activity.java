package org.example.tnal_youth_backend.activity.activity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "activities",
        indexes = {
                @Index(
                        name = "idx_activities_branch_id",
                        columnList = "branch_id"
                ),
                @Index(
                        name = "idx_activities_status_id",
                        columnList = "status_id"
                ),
                @Index(
                        name = "idx_activities_type_id",
                        columnList = "type_id"
                ),
                @Index(
                        name = "idx_activities_sector_id",
                        columnList = "sector_id"
                ),
                @Index(
                        name = "idx_activities_starts_at",
                        columnList = "starts_at"
                )
        }
)
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

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    /*
     * type_id → activity_types.id
     */
    @Column(
            name = "type_id",
            nullable = false
    )
    private Short typeId;

    /*
     * sector_id → activity_sectors.id
     */
    @Column(
            name = "sector_id",
            nullable = false
    )
    private Short sectorId;

    /*
     * status_id → activity_statuses.id
     */
    @Column(
            name = "status_id",
            nullable = false
    )
    private Short statusId;

    /*
     * branch_id → branches.id
     */
    @Column(
            name = "branch_id",
            nullable = false
    )
    private Long branchId;

    @Column(
            name = "is_public",
            nullable = false
    )
    private Boolean isPublic;

    @Column(
            name = "starts_at",
            nullable = false
    )
    private OffsetDateTime startsAt;

    @Column(
            name = "ends_at",
            nullable = false
    )
    private OffsetDateTime endsAt;

    /*
     * province_id → provinces.id
     */
    @Column(name = "province_id")
    private Short provinceId;

    /*
     * district_id → districts.id
     */
    @Column(name = "district_id")
    private Integer districtId;

    /*
     * commune_id → communes.id
     */
    @Column(name = "commune_id")
    private Integer communeId;

    @Column(
            name = "location_name",
            length = 255
    )
    private String locationName;

    @Column(
            name = "address",
            columnDefinition = "TEXT"
    )
    private String address;

    @Column(
            name = "google_map_url",
            columnDefinition = "TEXT"
    )
    private String googleMapUrl;

    @Column(name = "capacity")
    private Integer capacity;

    /*
     * cover_image_id → files.id
     */
    @Column(name = "cover_image_id")
    private Long coverImageId;

    /*
     * created_by → users.id
     */
    @Column(
            name = "created_by",
            nullable = false,
            updatable = false
    )
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
        OffsetDateTime now = OffsetDateTime.now();

        if (isPublic == null) {
            isPublic = false;
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