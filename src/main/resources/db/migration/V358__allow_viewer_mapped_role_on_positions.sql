-- Positions can now also map to VIEWER (a member-linked "Member Viewer"
-- account -- see MemberPasswordServiceImpl's VIEWER promotion branch and
-- AdminLookupServiceImpl's POSITION_MAPPED_ROLES). The CHECK constraint
-- from V339 was never widened for this, so the app-level validation in
-- AdminLookupServiceImpl#normalizeMappedRole would pass but the actual
-- INSERT/UPDATE of a VIEWER-mapped position would still fail here.
ALTER TABLE positions
    DROP CONSTRAINT chk_positions_mapped_role;

ALTER TABLE positions
    ADD CONSTRAINT chk_positions_mapped_role
    CHECK (mapped_role IS NULL OR mapped_role IN ('BRANCH_LEADER', 'SECRETARY', 'MEMBER', 'VIEWER'));
