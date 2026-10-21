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
            name = "branch_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String branchCode;

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

    // Null only for the single COUNTRY-level branch, which covers every
    // province (see V364); every other level requires one.
    @Column(name = "province_id")
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

    // For the branch report: whether the branch has a location of its own
    // ("មានទីតាំង") and whether it has been registered according to the law.
    // Every branch starts as both; V365 backfills the existing ones.
    @Builder.Default
    @Column(name = "has_location", nullable = false)
    private boolean hasLocation = true;

    @Builder.Default
    @Column(name = "legally_registered", nullable = false)
    private boolean legallyRegistered = true;

    // The registration document, optional, kept only while the branch is
    // registered by law.
    @Column(name = "legal_document_file_id")
    private Long legalDocumentFileId;

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