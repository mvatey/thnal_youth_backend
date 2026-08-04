-- ============================================================
-- DASHBOARD PARTICIPATION TREND TEST DATA
-- ============================================================

INSERT INTO activity_participants (
    activity_id,
    member_id,
    registered_at,
    invited_by,
    registration_source,
    created_at,
    updated_at
)
SELECT
    a.id,
    m.id,
    a.starts_at - INTERVAL '5 days',
    u.id,
    'MANUAL',
    a.starts_at - INTERVAL '5 days',
    a.starts_at - INTERVAL '5 days'
FROM activities a
    JOIN members m
ON m.member_no IN (
    'DASH-TEST-M001',
    'DASH-TEST-M002',
    'DASH-TEST-M003'
    )
    JOIN users u
    ON u.id = (
    SELECT MIN(id)
    FROM users
    )
WHERE a.title_en IN (
    'Dashboard Test Activity June 1',
    'Dashboard Test Activity June 2',
    'Dashboard Test Activity July 1',
    'Future Dashboard Test Activity'
    )
ON CONFLICT (activity_id, member_id)
    DO NOTHING;