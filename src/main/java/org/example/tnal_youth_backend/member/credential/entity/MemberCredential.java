package org.example.tnal_youth_backend.member.credential.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
                        name = "uk_member_credentials_credential_no",
                        columnNames = "credential_no"
                )
        },
        indexes = {
                @Index(
                        name = "idx_member_credentials_member_id",
                        columnList = "member_id"
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
     * Foreign Key IDs
     * ==========================================================
     */

    @Column(
            name = "member_id",
            nullable = false
    )
    private Long memberId;

    @Column(name = "issued_by")
    private Long issuedById;

    @Column(name = "file_id")
    private Long fileId;

    /*
     * ==========================================================
     * Credential Information
     * ==========================================================
     */

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            name = "credential_kind",
            nullable = false,
            length = 100
    )
    private String credentialKind;

    @Column(
            name = "credential_no",
            unique = true,
            length = 150
    )
    private String credentialNo;

    @Column(name = "issued_on")
    private LocalDate issuedOn;

    /*
     * ==========================================================
     * Read-only Relationships
     * ==========================================================
     *
     * The ID fields above remain responsible for insert/update.
     * These relationships are only used when building API responses.
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