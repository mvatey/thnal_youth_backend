package org.example.tnal_youth_backend.member.credential.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.member.entity.Member;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "member_credentials",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "member_credentials_credential_no_key",
                        columnNames = "credential_no"
                )
        },
        indexes = {
                @Index(
                        name = "idx_member_credentials_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_member_credentials_activity_id",
                        columnList = "activity_id"
                ),
                @Index(
                        name = "idx_member_credentials_issued_by",
                        columnList = "issued_by"
                ),
                @Index(
                        name = "idx_member_credentials_file_id",
                        columnList = "file_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ==========================================================
     * Foreign-key IDs
     * ==========================================================
     */

    @Column(
            name = "member_id",
            nullable = false
    )
    private Long memberId;

    /**
     * Required only for ACTIVITY_CERTIFICATE.
     *
     * Must be null for:
     * - MEMBERSHIP_CARD
     * - APPOINTMENT_LETTER
     */
    @Column(name = "activity_id")
    private Long activityId;

    @Column(
            name = "issued_by",
            nullable = false
    )
    private Long issuedById;

    @Column(name = "file_id")
    private Long fileId;

    /*
     * ==========================================================
     * Credential information
     * ==========================================================
     */

    @Column(
            name = "credential_kind",
            nullable = false,
            length = 30
    )
    private String credentialKind;

    @Column(
            name = "credential_no",
            nullable = false,
            unique = true,
            length = 100
    )
    private String credentialNo;

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            name = "issued_on",
            nullable = false
    )
    private LocalDate issuedOn;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private CredentialStatus status =
            CredentialStatus.ACTIVE;

    /*
     * ==========================================================
     * Read-only relationships
     * ==========================================================
     *
     * The ID fields above are responsible for inserts and updates.
     * These relationships are used only when reading and mapping
     * API responses.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            insertable = false,
            updatable = false
    )
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "activity_id",
            insertable = false,
            updatable = false
    )
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "issued_by",
            insertable = false,
            updatable = false
    )
    private User issuedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "file_id",
            insertable = false,
            updatable = false
    )
    private FileEntity file;

    /*
     * ==========================================================
     * Metadata
     * ==========================================================
     */

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

        if (status == null) {
            status =
                    CredentialStatus.ACTIVE;
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