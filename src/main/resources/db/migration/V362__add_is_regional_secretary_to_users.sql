-- A standalone SECRETARY account can now also be "regional" -- same
-- live-computed province/district branch coverage a member-linked
-- secretary gets via a SECRETARY_REGIONAL-mapped position (see
-- StaffBranchScopeService), except a standalone account has no Position
-- concept to carry that marker on, so it lives directly on the account
-- instead. users.branch_id is the anchor; this column is just the flag.
ALTER TABLE users
    ADD COLUMN is_regional_secretary BOOLEAN NOT NULL DEFAULT FALSE;
