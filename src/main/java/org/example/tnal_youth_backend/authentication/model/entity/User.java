package org.example.tnal_youth_backend.authentication.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * users.member_id → members.id
     */
    @Column(
            name = "member_id",
            unique = true
    )
    private Long memberId;

    @Column(
            name = "phone",
            unique = true,
            length = 30
    )
    private String phone;

    /*
     * Database type is CITEXT.
     * Java still uses String.
     */
    @Column(
            name = "email",
            unique = true,
            columnDefinition = "citext"
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String passwordHash;

    /*
     * users.role_id → roles.id
     */
    @Column(
            name = "role_id",
            nullable = false
    )
    private Short roleId;

    /*
     * users.account_status_id → account_statuses.id
     */
    @Column(
            name = "account_status_id",
            nullable = false
    )
    private Short accountStatusId;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(
            name = "failed_login_count",
            nullable = false
    )
    private Integer failedLoginCount;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    /*
     * users.created_by → users.id
     */
    @Column(name = "created_by")
    private Long createdById;

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

        if (failedLoginCount == null) {
            failedLoginCount = 0;
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
        updatedAt = OffsetDateTime.now();
    }
}