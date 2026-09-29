package org.example.tnal_youth_backend.authentication.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * One branch a standalone (non-member-linked) SECRETARY account covers.
 * Mirrors what {@code branch_staff} already does for a member-linked
 * secretary, but without any of its position/leadership fields, which
 * have no meaning here: no member record, no position, no is_primary
 * leadership slot. {@code users.branch_id} stays the account's single
 * "home" branch (still read directly in several places); this table
 * holds the full covered-branch list, including a row for that same
 * home branch, so it's the single authoritative source of the whole
 * list -- see {@code StaffBranchScopeService#secretaryBranchIds}.
 */
@Entity
@Table(name = "user_branch_assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBranchAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
