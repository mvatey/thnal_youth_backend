package org.example.tnal_youth_backend.lookup.dto;

/**
 * mappedRole is the system role (BRANCH_LEADER / SECRETARY / MEMBER /
 * VIEWER) a member holding this position is auto-assigned at creation —
 * null when this position has no auto-assigned role. See
 * MemberServiceImpl#createMember, which falls back to MEMBER in that case.
 * mappedViewerScope is only meaningful when mappedRole is VIEWER -- which
 * branch-scoped level (BRANCH_LEADER or SECRETARY) the resulting
 * member-linked viewer is assigned.
 */
public record PositionOptionResponse(
        Short id,
        String code,
        String labelKm,
        String labelEn,
        String mappedRole,
        String mappedViewerScope
) {
}
