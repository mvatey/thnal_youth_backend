package org.example.tnal_youth_backend.authentication.model.enums;

public enum UserRole {

    ADMIN,
    BRANCH_LEADER,
    SECRETARY,
    MEMBER,

    /*
     * Read-only, non-branch-linked account.
     *
     * VIEWER has the same viewing authority as ADMIN across the
     * modules this application already exposes to ADMIN, but is
     * never granted access to any create/update/delete endpoint.
     *
     * This is enforced by omission: VIEWER is intentionally left out
     * of every hasAnyRole(...) / @PreAuthorize(...) list that guards
     * a mutating (POST/PUT/PATCH/DELETE) endpoint anywhere in the
     * codebase. Do not add VIEWER to any such list.
     */
    VIEWER

}
