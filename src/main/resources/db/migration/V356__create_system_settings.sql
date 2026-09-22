-- Single-row settings table for values that used to be hardcoded backend
-- constants but need to be admin-editable without a redeploy -- starting
-- with the shared default password given to newly-created member-linked
-- accounts (previously PasswordPolicy.DEFAULT_MEMBER_PASSWORD).
CREATE TABLE system_settings (
    id SMALLINT PRIMARY KEY DEFAULT 1,
    default_member_password VARCHAR(255) NOT NULL,
    updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_system_settings_singleton CHECK (id = 1)
);

INSERT INTO system_settings (id, default_member_password, updated_at)
VALUES (1, 'Tnal@123', now());
