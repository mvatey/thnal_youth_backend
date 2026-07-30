package org.example.tnal_youth_backend.document.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.activity.activity.entity.Activity;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.document.type.enums.DocumentTypeCode;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.member.entity.Member;

import java.time.LocalDate;
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
                ),
                @Index(
                        name = "idx_documents_created_at",
                        columnList = "created_at"
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
     * Writable foreign-key values.
     *
     * The relationship objects below are read-only because the same database
     * columns are already mapped here.
     */

    @Column(
            name = "document_type_id",
            nullable = false
    )
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

    @Column(
            name = "uploaded_by",
            nullable = false
    )
    private Long uploadedById;

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
     * Read-only relationships.
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_type_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_documents_document_type"
            )
    )
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "file_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_documents_file"
            )
    )
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "branch_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_documents_branch"
            )
    )
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_documents_member"
            )
    )
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "activity_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_documents_activity"
            )
    )
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "uploaded_by",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_documents_uploaded_by"
            )
    )
    private User uploadedBy;

    @Column(name = "document_date")
    private LocalDate documentDate;

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

        normalizeText();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        normalizeText();
        updatedAt = OffsetDateTime.now();
    }

    /**
     * Clears all possible document owners.
     *
     * The service calls this before assigning the correct owner according
     * to the selected document type.
     */
    public void clearOwners() {
        branchId = null;
        memberId = null;
        activityId = null;
    }

    public void assignOrganizationOwner() {
        clearOwners();
    }

    public void assignBranchOwner(Long newBranchId) {
        clearOwners();
        branchId = newBranchId;
    }

    public void assignActivityOwner(Long newActivityId) {
        clearOwners();
        activityId = newActivityId;
    }

    public void assignMemberOwner(Long newMemberId) {
        clearOwners();
        memberId = newMemberId;
    }

    public void assignMemberOwner(Long newMemberId, Long newBranchId) {
        clearOwners();
        memberId = newMemberId;
        branchId = newBranchId;
    }

    public void assignActivityCertificateOwner(
            Long newMemberId,
            Long newActivityId
    ) {
        clearOwners();
        memberId = newMemberId;
        activityId = newActivityId;
    }

    @Transient
    public DocumentTypeCode getTypeCode() {
        if (documentType == null) {
            return null;
        }

        return documentType.getTypeCode();
    }

    @Transient
    public boolean isInstitutionalDocument() {
        DocumentTypeCode typeCode = getTypeCode();

        return typeCode != null
                && typeCode.isInstitutional();
    }

    @Transient
    public boolean isMemberDocument() {
        DocumentTypeCode typeCode = getTypeCode();

        return typeCode != null
                && typeCode.isMember();
    }

    @Transient
    public boolean hasOrganizationOwner() {
        return branchId == null
                && memberId == null
                && activityId == null;
    }

    @Transient
    public boolean hasBranchOwner() {
        return branchId != null
                && memberId == null
                && activityId == null;
    }

    @Transient
    public boolean hasActivityOwner() {
        return activityId != null
                && memberId == null;
    }

    @Transient
    public boolean hasMemberOwner() {
        return memberId != null;
    }

    private void normalizeText() {
        if (title != null) {
            title = title.trim();
        }

        if (description != null) {
            description = trimToNull(description);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isEmpty()
                ? null
                : trimmedValue;
    }
}