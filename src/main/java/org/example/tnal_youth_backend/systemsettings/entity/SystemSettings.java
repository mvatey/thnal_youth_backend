package org.example.tnal_youth_backend.systemsettings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/*
 * Single-row settings table -- id is always 1 (see V356 migration's
 * chk_system_settings_singleton check), same pattern as
 * organization.entity.OrganizationProfile.
 */
@Entity
@Table(name = "system_settings")
@Getter
@Setter
public class SystemSettings {

    @Id
    private Short id = 1;

    @Column(name = "default_member_password", nullable = false, length = 255)
    private String defaultMemberPassword;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = 1;
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
