package org.example.tnal_youth_backend.lookup.dto;

/**
 * mappedRole is the system role (BRANCH_LEADER / SECRETARY / MEMBER) a
 * member holding this position is auto-assigned at creation — null when
 * this position has no auto-assigned role. See
 * MemberServiceImpl#createMember, which falls back to MEMBER in that case.
 */
public record PositionOptionResponse(
        Short id,
        String code,
        String labelKm,
        String labelEn,
        String mappedRole
) {
}
