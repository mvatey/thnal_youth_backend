package org.example.tnal_youth_backend.document.document.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.activity.activity.entity.Activity;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.member.entity.Member;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "documents",
        indexes = {
                @Index(
                        name = "idx_documents_document_type_id",
                        columnList = "document_type_id"
                ),
                @Index(
                        name = "idx_documents_file_id",
                        columnList = "file_id"
                ),
                @Index(
                        name = "idx_documents_branch_id",
                        columnList = "branch_id"
                ),
                @Index(
                        name = "idx_documents_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_documents_activity_id",
                        columnList = "activity_id"
                ),
                @Index(
                        name = "idx_documents_uploaded_by",
                        columnList = "uploaded_by"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ==========================================================
     * Foreign Key IDs
     * ==========================================================
     */

    @Column(name = "document_type_id")
    private Short typeId;

    @Column(
            name = "file_id",
            nullable = false
    )
    private Long fileId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "uploaded_by")
    private Long uploadedById;

    /*
     * ==========================================================
     * Read-only Relationships
     * ==========================================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_type_id",
            insertable = false,
            updatable = false
    )
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "file_id",
            insertable = false,
            updatable = false
    )
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "branch_id",
            insertable = false,
            updatable = false
    )
    private Branch branch;

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
            name = "uploaded_by",
            insertable = false,
            updatable = false
    )
    private User uploadedBy;

    /*
     * ==========================================================
     * Document Information
     * ==========================================================
     */

    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

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