-- A position (e.g. "Branch Leader", "Secretary", "Member", "Support") can be
-- configured to auto-assign a system role when a member is created holding
-- it -- see MemberServiceImpl#createMember. NULL means "no auto role"; the
-- create flow falls back to MEMBER in that case, same as before this column
-- existed.
ALTER TABLE positions
    ADD COLUMN IF NOT EXISTS mapped_role VARCHAR(30);

ALTER TABLE positions
    ADD CONSTRAINT chk_positions_mapped_role
    CHECK (mapped_role IS NULL OR mapped_role IN ('BRANCH_LEADER', 'SECRETARY', 'MEMBER'));

-- The 3 seeded positions already share their literal role name as their
-- code (see V1) -- wire that up now instead of leaving every existing
-- position unmapped.
UPDATE positions
SET mapped_role = code
WHERE code IN ('BRANCH_LEADER', 'SECRETARY', 'MEMBER');
