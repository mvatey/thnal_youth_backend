ALTER TABLE users
    ADD COLUMN IF NOT EXISTS viewer_scope VARCHAR(30);

ALTER TABLE users
    ADD CONSTRAINT chk_users_viewer_scope
    CHECK (viewer_scope IS NULL OR viewer_scope IN ('ADMIN','BRANCH_LEADER','SECRETARY','MEMBER'));
