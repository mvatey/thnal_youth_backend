ALTER TABLE password_reset_tokens
    DROP CONSTRAINT IF EXISTS chk_password_reset_consumed_at;

ALTER TABLE password_reset_tokens
    ADD CONSTRAINT chk_password_reset_consumed_at
        CHECK (
            consumed_at IS NULL
                OR consumed_at >= created_at
            );