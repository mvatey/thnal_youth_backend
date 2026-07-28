-- ============================================================
-- 3. ACTIVITIES
-- Two activities start in June.
-- Two additional activities start in July.
-- ============================================================

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
    'សកម្មភាពសាកល្បងខែមិថុនា ១',
    'Dashboard Test Activity June 1',
    'Dashboard summary test activity',
    at.id,
    asector.id,
    ast.id,
    1,
    true,
    TIMESTAMPTZ '2026-06-05 08:00:00+07',
    TIMESTAMPTZ '2026-06-05 11:00:00+07',
    'Head Office',
    u.id,
    TIMESTAMPTZ '2026-06-01 09:00:00+07',
    TIMESTAMPTZ '2026-06-01 09:00:00+07'
FROM activity_types at
JOIN activity_statuses ast
ON ast.code = 'COMPLETED'
    JOIN activity_sectors asector
    ON TRUE
    JOIN users u
    ON TRUE
WHERE at.code = 'INTERNAL'
  AND asector.id = (
    SELECT MIN(id)
    FROM activity_sectors
    )
  AND u.id = (
    SELECT MIN(id)
    FROM users
    )
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Test Activity June 1'
    );


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
    'សកម្មភាពសាកល្បងខែមិថុនា ២',
    'Dashboard Test Activity June 2',
    'Dashboard summary test activity',
    at.id,
    asector.id,
    ast.id,
    1,
    true,
    TIMESTAMPTZ '2026-06-18 08:00:00+07',
    TIMESTAMPTZ '2026-06-18 11:00:00+07',
    'Head Office',
    u.id,
    TIMESTAMPTZ '2026-06-10 09:00:00+07',
    TIMESTAMPTZ '2026-06-10 09:00:00+07'
FROM activity_types at
JOIN activity_statuses ast
ON ast.code = 'COMPLETED'
    JOIN activity_sectors asector
    ON TRUE
    JOIN users u
    ON TRUE
WHERE at.code = 'EXTERNAL'
  AND asector.id = (
    SELECT MIN(id)
    FROM activity_sectors
    )
  AND u.id = (
    SELECT MIN(id)
    FROM users
    )
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Test Activity June 2'
    );


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
    'សកម្មភាពសាកល្បងខែកក្កដា ១',
    'Dashboard Test Activity July 1',
    'Dashboard summary test activity',
    at.id,
    asector.id,
    ast.id,
    1,
    true,
    TIMESTAMPTZ '2026-07-08 08:00:00+07',
    TIMESTAMPTZ '2026-07-08 11:00:00+07',
    'Head Office',
    u.id,
    TIMESTAMPTZ '2026-07-01 09:00:00+07',
    TIMESTAMPTZ '2026-07-01 09:00:00+07'
FROM activity_types at
JOIN activity_statuses ast
ON ast.code = 'COMPLETED'
    JOIN activity_sectors asector
    ON TRUE
    JOIN users u
    ON TRUE
WHERE at.code = 'INTERNAL'
  AND asector.id = (
    SELECT MIN(id)
    FROM activity_sectors
    )
  AND u.id = (
    SELECT MIN(id)
    FROM users
    )
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Test Activity July 1'
    );


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
    'សកម្មភាពសាកល្បងខែកក្កដា ២',
    'Dashboard Test Activity July 2',
    'Dashboard summary test activity',
    at.id,
    asector.id,
    ast.id,
    1,
    true,
    TIMESTAMPTZ '2026-07-22 08:00:00+07',
    TIMESTAMPTZ '2026-07-22 11:00:00+07',
    'Head Office',
    u.id,
    TIMESTAMPTZ '2026-07-10 09:00:00+07',
    TIMESTAMPTZ '2026-07-10 09:00:00+07'
FROM activity_types at
JOIN activity_statuses ast
ON ast.code = 'UPCOMING'
    JOIN activity_sectors asector
    ON TRUE
    JOIN users u
    ON TRUE
WHERE at.code = 'EXTERNAL'
  AND asector.id = (
    SELECT MIN(id)
    FROM activity_sectors
    )
  AND u.id = (
    SELECT MIN(id)
    FROM users
    )
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Test Activity July 2'
    );

-- ============================================================
-- 4. DONATIONS
-- June totals:
--   KHR 100,000
--   USD 50
--
-- July totals:
--   KHR 150,000
--   USD 75
-- ============================================================

INSERT INTO donations (
    donation_no,
    donation_type_id,
    donor_name,
    branch_id,
    donation_period,
    amount_khr,
    amount_usd,
    exchange_rate_khr_per_usd,
    total_amount_usd,
    payment_method_id,
    paid_at,
    recorded_by,
    note,
    created_at,
    updated_at
)
SELECT
    'DASH-TEST-DON-2026-06',
    dt.id,
    'Dashboard Test Donor June',
    1,
    DATE '2026-06-01',
    100000.00,
    50.00,
    4000.0000,
    75.00,
    pm.id,
    TIMESTAMPTZ '2026-06-15 10:00:00+07',
    u.id,
    'Dashboard test donation for June 2026',
    TIMESTAMPTZ '2026-06-15 10:00:00+07',
    TIMESTAMPTZ '2026-06-15 10:00:00+07'
FROM donation_types dt
         JOIN payment_methods pm
              ON pm.id = (
                  SELECT MIN(id)
                  FROM payment_methods
              )
         JOIN users u
              ON u.id = (
                  SELECT MIN(id)
                  FROM users
              )
WHERE dt.code = 'MONTHLY_DONATION'
  AND NOT EXISTS (
    SELECT 1
    FROM donations
    WHERE donation_no = 'DASH-TEST-DON-2026-06'
);


INSERT INTO donations (
    donation_no,
    donation_type_id,
    donor_name,
    branch_id,
    donation_period,
    amount_khr,
    amount_usd,
    exchange_rate_khr_per_usd,
    total_amount_usd,
    payment_method_id,
    paid_at,
    recorded_by,
    note,
    created_at,
    updated_at
)
SELECT
    'DASH-TEST-DON-2026-07',
    dt.id,
    'Dashboard Test Donor July',
    1,
    DATE '2026-07-01',
    150000.00,
    75.00,
    4000.0000,
    112.50,
    pm.id,
    TIMESTAMPTZ '2026-07-15 10:00:00+07',
    u.id,
    'Dashboard test donation for July 2026',
    TIMESTAMPTZ '2026-07-15 10:00:00+07',
    TIMESTAMPTZ '2026-07-15 10:00:00+07'
FROM donation_types dt
         JOIN payment_methods pm
              ON pm.id = (
                  SELECT MIN(id)
                  FROM payment_methods
              )
         JOIN users u
              ON u.id = (
                  SELECT MIN(id)
                  FROM users
              )
WHERE dt.code = 'SPONSOR_DONATION'
  AND NOT EXISTS (
    SELECT 1
    FROM donations
    WHERE donation_no = 'DASH-TEST-DON-2026-07'
);