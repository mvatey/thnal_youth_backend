package org.example.tnal_youth_backend.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.example.tnal_youth_backend.file.entity.FileEntity;

import java.time.OffsetDateTime;

@Entity
@Table(name = "organization_profile")
@Getter
@Setter
public class OrganizationProfile {

    @Id
    private Short id = 1;

    @Column(name = "name_km", nullable = false, length = 255)
    private String nameKm;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(name = "tagline_km", length = 255)
    private String taglineKm;

    @Column(name = "tagline_en", length = 255)
    private String taglineEn;

    @Column(name = "about_km", columnDefinition = "TEXT")
    private String aboutKm;

    @Column(name = "about_en", columnDefinition = "TEXT")
    private String aboutEn;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 30)
    private String phone;

    @Column(columnDefinition = "citext")
    private String email;

    @Column(length = 500)
    private String website;

    @Column(name = "facebook_url", length = 500)
    private String facebookUrl;

    @Column(name = "telegram_url", length = 500)
    private String telegramUrl;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_file_id")
    private FileEntity logoFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_file_id")
    private FileEntity coverFile;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (id == null) {
            id = 1;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
