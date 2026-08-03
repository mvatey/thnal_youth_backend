package org.example.tnal_youth_backend.activity.media.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.tnal_youth_backend.activity.model.entity.Activity;
import org.example.tnal_youth_backend.file.entity.FileEntity;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "activity_photos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_activity_photo",
                        columnNames = {
                                "activity_id",
                                "file_id"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "activity_id",
            nullable = false
    )
    private Activity activity;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "file_id",
            nullable = false
    )
    private FileEntity file;

    @Column(
            name = "caption",
            length = 255
    )
    private String caption;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder;

    @Column(
            name = "uploaded_by",
            nullable = false
    )
    private Long uploadedBy;

    @Column(
            name = "uploaded_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        if (sortOrder == null) {
            sortOrder = 0;
        }

        if (uploadedAt == null) {
            uploadedAt = OffsetDateTime.now();
        }
    }
}