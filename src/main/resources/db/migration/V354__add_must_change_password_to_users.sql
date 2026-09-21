-- Tracks whether a member-linked account is still on the shared default
-- password (see PasswordPolicy.DEFAULT_MEMBER_PASSWORD) and must set a
-- real one before using the app. Defaults false for every existing row --
-- this only applies going forward, to newly-created member accounts.
ALTER TABLE users
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
