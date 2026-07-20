package org.example.tnal_youth_backend.file.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "file_path",
            nullable = false,
            unique = true,
            columnDefinition = "TEXT"
    )
    private String filePath;

    @Column(
            name = "original_name",
            nullable = false,
            length = 255
    )
    private String originalName;

    @Column(
            name = "mime_type",
            nullable = false,
            length = 100
    )
    private String mimeType;

    @Column(
            name = "size_bytes",
            nullable = false
    )
    private Long sizeBytes;

    /*
     * Flyway foreign key:
     *
     * files.uploaded_by → users.id
     * ON DELETE SET NULL
     *
     * It is stored as an ID so this module does not depend
     * on the authentication package.
     */
    @Column(name = "uploaded_by")
    private Long uploadedById;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}