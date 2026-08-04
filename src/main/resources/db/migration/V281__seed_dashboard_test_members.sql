-- ============================================================
-- DASHBOARD TEST MEMBERS
-- Branch: Head Office, ID = 1
--
-- Expected:
-- Through June 2026 = 2 active members
-- Through July 2026 = 3 active members
-- July change = 50%
-- ============================================================

INSERT INTO members (
    member_no,
    full_name_km,
    full_name_en,
    branch_id,
    status_id,
    gender,
    joined_on,
    created_at,
    updated_at
)
VALUES
    (
        'DASH-TEST-M001',
        'សមាជិកសាកល្បងទី១',
        'Dashboard Test Member 1',
        1,
        1,
        'MALE',
        DATE '2026-06-05',
        TIMESTAMPTZ '2026-06-05 09:00:00+07',
        TIMESTAMPTZ '2026-06-05 09:00:00+07'
    ),
    (
        'DASH-TEST-M002',
        'សមាជិកសាកល្បងទី២',
        'Dashboard Test Member 2',
        1,
        1,
        'FEMALE',
        DATE '2026-06-20',
        TIMESTAMPTZ '2026-06-20 09:00:00+07',
        TIMESTAMPTZ '2026-06-20 09:00:00+07'
    ),
    (
        'DASH-TEST-M003',
        'សមាជិកសាកល្បងទី៣',
        'Dashboard Test Member 3',
        1,
        1,
        'MALE',
        DATE '2026-07-12',
        TIMESTAMPTZ '2026-07-12 09:00:00+07',
        TIMESTAMPTZ '2026-07-12 09:00:00+07'
    )
    ON CONFLICT (member_no) DO NOTHING;