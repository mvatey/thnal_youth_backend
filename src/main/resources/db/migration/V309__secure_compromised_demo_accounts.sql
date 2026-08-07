-- Remove the publicly known demo password from every account.
-- The designated production administrator must complete the existing
-- email-OTP activation flow to choose a new private password.
DO $$
DECLARE
    production_admin_email CONSTANT TEXT :=
        'thavryvateyphal113@gmail.com';
    compromised_password_hash CONSTANT TEXT :=
        '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti';
    unusable_replacement_hash CONSTANT TEXT :=
        '$2a$12$vSacT/ew7jUt7XDf0JI.A.ob64GNsVcBR.0FlqyBKoOkPo6nR6F7q';
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM users
        WHERE LOWER(email) = LOWER(production_admin_email)
    ) THEN
        RAISE EXCEPTION
            'Designated production administrator % was not found',
            production_admin_email;
    END IF;

    UPDATE refresh_tokens
    SET revoked_at = COALESCE(revoked_at, CURRENT_TIMESTAMP)
    WHERE user_id IN (
        SELECT id
        FROM users
        WHERE password_hash = compromised_password_hash
    );

    UPDATE users
    SET role = 'ADMIN',
        status = 'PENDING_ACTIVATION',
        password_hash = unusable_replacement_hash,
        activated_at = NULL,
        failed_login_count = 0,
        locked_until = NULL,
        updated_at = CURRENT_TIMESTAMP
    WHERE LOWER(email) = LOWER(production_admin_email);

    UPDATE users
    SET status = 'INACTIVE',
        password_hash = unusable_replacement_hash,
        failed_login_count = 0,
        locked_until = NULL,
        updated_at = CURRENT_TIMESTAMP
    WHERE password_hash = compromised_password_hash;
END
$$;
