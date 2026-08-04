DO $$
DECLARE
target_member_id BIGINT := 11;
    issuer_user_id BIGINT := 1;
    certificate_activity_id BIGINT;
BEGIN
    /*
     * Verify the selected member exists.
     */
    IF NOT EXISTS (
        SELECT 1
        FROM members
        WHERE id = target_member_id
    ) THEN
        RAISE EXCEPTION
            'Member % was not found',
            target_member_id;
END IF;

    /*
     * Verify the issuing user exists.
     */
    IF NOT EXISTS (
        SELECT 1
        FROM users
        WHERE id = issuer_user_id
    ) THEN
        RAISE EXCEPTION
            'Issuer user % was not found',
            issuer_user_id;
END IF;

    /*
     * Select an existing activity for activity certificates.
     */
SELECT id
INTO certificate_activity_id
FROM activities
ORDER BY id
    LIMIT 1;

IF certificate_activity_id IS NULL THEN
        RAISE EXCEPTION
            'No activity exists for seeding activity certificates';
END IF;

    /*
     * ==========================================================
     * MEMBERSHIP CARD
     * One membership card for this member.
     *
     * Database rule:
     * MEMBERSHIP_CARD -> activity_id must be NULL
     * ==========================================================
     */
INSERT INTO member_credentials (
    member_id,
    activity_id,
    credential_kind,
    credential_no,
    title,
    issued_on,
    issued_by,
    file_id,
    created_at,
    updated_at
)
SELECT
    target_member_id,
    NULL,
    'MEMBERSHIP_CARD',
    'CARD-00012',
    'ប័ណ្ណសម្គាល់សមាជិក',
    DATE '2026-07-28',
    issuer_user_id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1
        FROM member_credentials
        WHERE member_id = target_member_id
          AND credential_kind = 'MEMBERSHIP_CARD'
    );

/*
 * ==========================================================
 * ACTIVITY CERTIFICATE 1
 *
 * Database rule:
 * ACTIVITY_CERTIFICATE -> activity_id is required
 * ==========================================================
 */
INSERT INTO member_credentials (
    member_id,
    activity_id,
    credential_kind,
    credential_no,
    title,
    issued_on,
    issued_by,
    file_id,
    created_at,
    updated_at
)
SELECT
    target_member_id,
    certificate_activity_id,
    'ACTIVITY_CERTIFICATE',
    'CERT-00012-01',
    'បណ្ណសរសើរសកម្មភាពទី១',
    DATE '2026-07-28',
    issuer_user_id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1
        FROM member_credentials
        WHERE credential_no = 'CERT-00012-01'
    );

/*
 * ==========================================================
 * ACTIVITY CERTIFICATE 2
 * ==========================================================
 */
INSERT INTO member_credentials (
    member_id,
    activity_id,
    credential_kind,
    credential_no,
    title,
    issued_on,
    issued_by,
    file_id,
    created_at,
    updated_at
)
SELECT
    target_member_id,
    certificate_activity_id,
    'ACTIVITY_CERTIFICATE',
    'CERT-00012-02',
    'បណ្ណសរសើរសកម្មភាពទី២',
    DATE '2026-07-28',
    issuer_user_id,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1
        FROM member_credentials
        WHERE credential_no = 'CERT-00012-02'
    );
END
$$;