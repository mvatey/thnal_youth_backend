package org.example.tnal_youth_backend.document.type.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.document.type.enums.DocumentScope;
import org.example.tnal_youth_backend.document.type.enums.DocumentTypeCode;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "document_types",
        indexes = {
                @Index(
                        name = "idx_document_types_code",
                        columnList = "code",
                        unique = true
                ),
                @Index(
                        name = "idx_document_types_active_sort",
                        columnList = "is_active, sort_order"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

    @Column(
            name = "label_km",
            nullable = false,
            length = 150
    )
    private String labelKm;

    @Column(
            name = "label_en",
            length = 150
    )
    private String labelEn;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder;

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

        normalizeValues();

        if (isActive == null) {
            isActive = true;
        }

        if (sortOrder == null) {
            sortOrder = 0;
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
        normalizeValues();
        updatedAt = OffsetDateTime.now();
    }

    /**
     * Converts the stored database code to its enum.
     *
     * Returns null only when the database contains an unsupported old value.
     */
    @Transient
    public DocumentTypeCode getTypeCode() {
        return DocumentTypeCode.fromCodeOrNull(code);
    }

    /**
     * Scope is derived from the document type code.
     *
     * It does not require a separate scope column in the database.
     */
    @Transient
    public DocumentScope getScope() {
        DocumentTypeCode typeCode = getTypeCode();

        return typeCode == null
                ? null
                : typeCode.getScope();
    }

    @Transient
    public boolean isInstitutionalType() {
        DocumentTypeCode typeCode = getTypeCode();

        return typeCode != null
                && typeCode.isInstitutional();
    }

    @Transient
    public boolean isMemberType() {
        DocumentTypeCode typeCode = getTypeCode();

        return typeCode != null
                && typeCode.isMember();
    }

    @Transient
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive);
    }

    private void normalizeValues() {
        if (code != null) {
            code = code.trim().toUpperCase();
        }

        if (labelKm != null) {
            labelKm = labelKm.trim();
        }

        if (labelEn != null) {
            labelEn = trimToNull(labelEn);
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