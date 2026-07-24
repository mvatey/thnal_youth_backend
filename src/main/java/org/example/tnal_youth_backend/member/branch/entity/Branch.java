package org.example.tnal_youth_backend.member.branch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "branches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "name_km",
            nullable = false,
            length = 255
    )
    private String nameKm;

    @Column(
            name = "name_en",
            length = 255
    )
    private String nameEn;

    @Column(
            name = "branch_level_id",
            nullable = false
    )
    private Short branchLevelId;

    @Column(name = "parent_branch_id")
    private Long parentBranchId;

    @Column(
            name = "province_id",
            nullable = false
    )
    private Short provinceId;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "commune_id")
    private Integer communeId;

    @Column(
            name = "status_id",
            nullable = false
    )
    private Short statusId;

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