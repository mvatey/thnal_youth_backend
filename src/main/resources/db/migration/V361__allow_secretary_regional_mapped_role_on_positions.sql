-- A position can now also map to SECRETARY_REGIONAL -- a member-linked
-- secretary whose branch coverage is computed live from an anchor
-- branch's province/district (see AdminLookupServiceImpl#
-- POSITION_MAPPED_ROLES and StaffBranchScopeService's regional-coverage
-- resolution), rather than a flat manually-picked branch list. The
-- account's actual role still resolves to plain SECRETARY -- this is a
-- position-level marker only, never a real UserRole.
ALTER TABLE positions
    DROP CONSTRAINT chk_positions_mapped_role;

ALTER TABLE positions
    ADD CONSTRAINT chk_positions_mapped_role
    CHECK (mapped_role IS NULL OR mapped_role IN ('BRANCH_LEADER', 'SECRETARY', 'SECRETARY_REGIONAL', 'MEMBER', 'VIEWER'));
