ALTER TABLE password_reset_tokens
    ADD COLUMN IF NOT EXISTS purpose
    VARCHAR(50);

UPDATE password_reset_tokens
SET purpose = 'PASSWORD_RESET'
WHERE purpose IS NULL;

ALTER TABLE password_reset_tokens
    ALTER COLUMN purpose SET NOT NULL;

ALTER TABLE password_reset_tokens
    ALTER COLUMN purpose
        SET DEFAULT 'PASSWORD_RESET';

ALTER TABLE password_reset_tokens
DROP CONSTRAINT IF EXISTS
        chk_password_reset_token_purpose;

ALTER TABLE password_reset_tokens
    ADD CONSTRAINT
        chk_password_reset_token_purpose
        CHECK (
            purpose IN (
                        'PASSWORD_RESET',
                        'ACCOUNT_ACTIVATION'
                )
            );

CREATE INDEX IF NOT EXISTS
    idx_password_reset_tokens_user_purpose
    ON password_reset_tokens (
    user_id,
    purpose
    );