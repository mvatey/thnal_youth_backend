ALTER TABLE member_credentials
DROP CONSTRAINT IF EXISTS chk_member_credential_kind;

ALTER TABLE member_credentials
    ADD CONSTRAINT chk_member_credential_kind
        CHECK (
            credential_kind IN (
                                'MEMBERSHIP_CARD',
                                'ACTIVITY_CERTIFICATE',
                                'APPOINTMENT_LETTER'
                )
            );

ALTER TABLE member_credentials
DROP CONSTRAINT IF EXISTS chk_member_credential_activity;

ALTER TABLE member_credentials
    ADD CONSTRAINT chk_member_credential_activity
        CHECK (
            (
                credential_kind = 'ACTIVITY_CERTIFICATE'
                    AND activity_id IS NOT NULL
                )
                OR
            (
                credential_kind IN (
                                    'MEMBERSHIP_CARD',
                                    'APPOINTMENT_LETTER'
                    )
                    AND activity_id IS NULL
                )
            );

ALTER TABLE member_credentials
    ADD COLUMN IF NOT EXISTS status VARCHAR(20)
    NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE member_credentials
DROP CONSTRAINT IF EXISTS chk_member_credential_status;

ALTER TABLE member_credentials
    ADD CONSTRAINT chk_member_credential_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'REVOKED'
                )
            );