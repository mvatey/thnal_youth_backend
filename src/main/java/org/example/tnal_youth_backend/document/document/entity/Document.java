package org.example.tnal_youth_backend.document.document.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "document_type_id")
    private Short typeId;

    @Column(
            name = "file_id",
            nullable = false
    )
    private Long fileId;

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

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "uploaded_by")
    private Long uploadedById;

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

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}