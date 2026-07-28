package org.example.tnal_youth_backend.authentication.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Links this login account to a member profile.
     *
     * Existing authentication test accounts may keep this value null.
     */
    @Column(
            name = "member_id",
            unique = true
    )
    private Long memberId;

    @Column(
            nullable = false,
            unique = true,
            length = 20
    )
    private String phone;

    @Column(unique = true)
    private String email;

    @Column(
            name = "password_hash",
            nullable = false
    )
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(
            name = "full_name_km",
            nullable = false
    )
    private String fullNameKm;

    @Column(name = "full_name_en")
    private String fullNameEn;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "failed_login_count")
    @Builder.Default
    private Integer failedLoginCount = 0;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(
            name = "created_at",
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
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

        if (failedLoginCount == null) {
            failedLoginCount = 0;
        }

        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + role.name()
                )
        );
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}