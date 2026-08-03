package org.example.tnal_youth_backend.member.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.level.entity.MemberLevel;
import org.example.tnal_youth_backend.member.religion.entity.Religion;
import org.example.tnal_youth_backend.member.status.entity.MemberStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "member_no",
            nullable = false,
            unique = true,
            length = 50
    )
    private String memberNo;

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

    /*
     * Temporary ID mapping.
     *
     * Database foreign key:
     * members.branch_id → branches.id
     */
    @Column(
            name = "branch_id",
            nullable = false
    )
    private Long branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "status_id",
            nullable = false
    )
    private MemberStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private MemberLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "religion_id")
    private Religion religion;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "gender",
            nullable = false,
            length = 20
    )
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(
            name = "place_of_birth",
            columnDefinition = "TEXT"
    )
    private String placeOfBirth;

    @Column(
            name = "phone",
            length = 30
    )
    private String phone;

    /*
     * PostgreSQL column type is CITEXT.
     */
    @Column(
            name = "email",
            columnDefinition = "citext"
    )
    private String email;

    @Column(
            name = "current_address",
            columnDefinition = "TEXT"
    )
    private String currentAddress;

    @Column(
            name = "permanent_address",
            columnDefinition = "TEXT"
    )
    private String permanentAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_photo_id")
    private FileEntity profilePhoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_file_id")
    private FileEntity cvFile;

    @Column(name = "joined_on")
    private LocalDate joinedOn;

    @Column(
            name = "bio",
            columnDefinition = "TEXT"
    )
    private String bio;

    /*
     * Temporary ID mapping.
     *
     * Database foreign key:
     * members.created_by → users.id
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