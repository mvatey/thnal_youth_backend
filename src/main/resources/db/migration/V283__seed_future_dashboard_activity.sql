INSERT INTO activities (
    title_km,
    title_en,
    description,
    type_id,
    sector_id,
    status_id,
    branch_id,
    is_public,
    starts_at,
    ends_at,
    location_name,
    created_by,
    created_at,
    updated_at
)
SELECT
    'សកម្មភាពសាកល្បងនាពេលខាងមុខ',
    'Future Dashboard Test Activity',
    'Future activity used to test the dashboard upcoming list',
    activity_type.id,
    activity_sector.id,
    activity_status.id,
    1,
    true,
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    CURRENT_TIMESTAMP + INTERVAL '7 days 3 hours',
    'Head Office',
    test_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM activity_types activity_type
    JOIN activity_statuses activity_status
ON activity_status.code = 'UPCOMING'
    JOIN activity_sectors activity_sector
    ON activity_sector.id = (
    SELECT MIN(id)
    FROM activity_sectors
    )
    JOIN users test_user
    ON test_user.id = (
    SELECT MIN(id)
    FROM users
    )
WHERE activity_type.code = 'INTERNAL'
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Future Dashboard Test Activity'
    );