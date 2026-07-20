package org.example.tnal_youth_backend.member.education.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.member.entity.Member;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "member_education",
        indexes = {
                @Index(
                        name = "idx_member_education_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_member_education_level_id",
                        columnList = "education_level_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEducation {

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
            name = "school_name",
            nullable = false,
            length = 255
    )
    private String schoolName;

    /*
     * Temporary ID mapping:
     * member_education.education_level_id
     * → education_levels.id
     */
    @Column(
            name = "education_level_id",
            nullable = false
    )
    private Short educationLevelId;

    @Column(
            name = "field_of_study",
            length = 255
    )
    private String fieldOfStudy;

    @Column(
            name = "country_code",
            length = 2
    )
    private String countryCode;

    @Column(
            name = "country_name",
            length = 100
    )
    private String countryName;

    /*
     * Temporary ID mapping:
     * member_education.province_id → provinces.id
     */
    @Column(name = "province_id")
    private Short provinceId;

    @Column(
            name = "province_name",
            length = 100
    )
    private String provinceName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_file_id")
    private FileEntity certificateFile;

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