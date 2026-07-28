DO $$
DECLARE
target_member_id BIGINT := 12;
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM members
        WHERE id = target_member_id
    ) THEN
        RAISE EXCEPTION
            'Member % was not found',
            target_member_id;
END IF;

INSERT INTO activity_participants (
    activity_id,
    member_id,
    attendance_status_id,
    registered_at,
    checked_in_at,
    checked_out_at,
    invited_by,
    invited_branch_id,
    registration_source,
    note,
    created_at,
    updated_at
)
SELECT
    5,
    target_member_id,
    1,
    TIMESTAMPTZ '2026-06-05 08:00:00+07',
    TIMESTAMPTZ '2026-06-05 08:10:00+07',
    TIMESTAMPTZ '2026-06-05 11:00:00+07',
    1,
    NULL,
    'MANUAL',
    'Test participation: present',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1
        FROM activity_participants
        WHERE activity_id = 5
          AND member_id = target_member_id
    );

INSERT INTO activity_participants (
    activity_id,
    member_id,
    attendance_status_id,
    registered_at,
    checked_in_at,
    checked_out_at,
    invited_by,
    invited_branch_id,
    registration_source,
    note,
    created_at,
    updated_at
)
SELECT
    6,
    target_member_id,
    2,
    TIMESTAMPTZ '2026-06-18 08:00:00+07',
    NULL,
    NULL,
    1,
    NULL,
    'MANUAL',
    'Test participation: absent',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1
        FROM activity_participants
        WHERE activity_id = 6
          AND member_id = target_member_id
    );

INSERT INTO activity_participants (
    activity_id,
    member_id,
    attendance_status_id,
    registered_at,
    checked_in_at,
    checked_out_at,
    invited_by,
    invited_branch_id,
    registration_source,
    note,
    created_at,
    updated_at
)
SELECT
    7,
    target_member_id,
    1,
    TIMESTAMPTZ '2026-07-08 08:00:00+07',
    TIMESTAMPTZ '2026-07-08 08:05:00+07',
    TIMESTAMPTZ '2026-07-08 11:30:00+07',
    1,
    NULL,
    'MANUAL',
    'Test participation: present',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1
        FROM activity_participants
        WHERE activity_id = 7
          AND member_id = target_member_id
    );

INSERT INTO activity_participants (
    activity_id,
    member_id,
    attendance_status_id,
    registered_at,
    checked_in_at,
    checked_out_at,
    invited_by,
    invited_branch_id,
    registration_source,
    note,
    created_at,
    updated_at
)
SELECT
    9,
    target_member_id,
    NULL,
    CURRENT_TIMESTAMP,
    NULL,
    NULL,
    1,
    NULL,
    'MANUAL',
    'Upcoming activity registration',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1
        FROM activity_participants
        WHERE activity_id = 9
          AND member_id = target_member_id
    );
END
$$;