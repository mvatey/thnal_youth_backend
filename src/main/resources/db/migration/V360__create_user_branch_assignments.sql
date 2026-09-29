-- A standalone (non-member-linked) SECRETARY account's branch coverage.
-- Mirrors what branch_staff already does for a member-linked secretary,
-- but without any of branch_staff's position/leadership fields, which
-- have no meaning for a bare login-only account: no member record, no
-- position, no is_primary leadership slot. users.branch_id stays the
-- account's single "home" branch (unchanged, still read directly in
-- several places); this table holds every branch the account covers,
-- including a row for that same home branch, so it's the single
-- authoritative source of the full list.
CREATE TABLE user_branch_assignments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_branch_assignments_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_branch_assignments_branch
        FOREIGN KEY (branch_id)
            REFERENCES branches(id)
            ON DELETE CASCADE,

    CONSTRAINT uq_user_branch_assignments
        UNIQUE (user_id, branch_id)
);

CREATE INDEX idx_user_branch_assignments_user_id
    ON user_branch_assignments(user_id);

CREATE INDEX idx_user_branch_assignments_branch_id
    ON user_branch_assignments(branch_id);
