-- ============================================================
-- V285__seed_dashboard_multi_branch_demo_data.sql
--
-- Dashboard multi-branch demonstration data
--
-- Covers:
--   1. Multiple active branches
--   2. Multi-branch branch_staff assignments
--   3. Active/inactive members
--   4. Monthly member growth
--   5. Completed/upcoming activities
--   6. Internal/external activity breakdown
--   7. Activity participation trend
--   8. KHR/USD monthly donation totals
-- ============================================================


-- ============================================================
-- 1. CREATE DEMO BRANCHES
-- ============================================================

INSERT INTO branches (
    branch_code,
    name_km,
    name_en,
    branch_level_id,
    parent_branch_id,
    province_id,
    status_id,
    address,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-BRANCH-A',
    'សាខាសាកល្បង ក',
    'Dashboard Demo Branch A',
    bl.id,
    parent_branch.id,
    province.id,
    branch_status.id,
    'Dashboard demonstration address A',
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branch_levels bl
         JOIN branch_statuses branch_status
              ON branch_status.code = 'ACTIVE'
         JOIN branches parent_branch
              ON parent_branch.id = (
                  SELECT MIN(id)
                  FROM branches
              )
         JOIN provinces province
              ON province.id = (
                  SELECT MIN(id)
                  FROM provinces
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE bl.code = 'PROVINCE'
  AND NOT EXISTS (
    SELECT 1
    FROM branches
    WHERE branch_code = 'DASH-DEMO-BRANCH-A'
);


INSERT INTO branches (
    branch_code,
    name_km,
    name_en,
    branch_level_id,
    parent_branch_id,
    province_id,
    status_id,
    address,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-BRANCH-B',
    'សាខាសាកល្បង ខ',
    'Dashboard Demo Branch B',
    bl.id,
    parent_branch.id,
    province.id,
    branch_status.id,
    'Dashboard demonstration address B',
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branch_levels bl
         JOIN branch_statuses branch_status
              ON branch_status.code = 'ACTIVE'
         JOIN branches parent_branch
              ON parent_branch.id = (
                  SELECT MIN(id)
                  FROM branches
              )
         JOIN provinces province
              ON province.id = (
                  SELECT id
                  FROM provinces
                  ORDER BY id
                  OFFSET 1
    LIMIT 1
    )
    JOIN users admin_user
ON admin_user.id = (
    SELECT MIN(id)
    FROM users
    WHERE role = 'ADMIN'
    )
WHERE bl.code = 'PROVINCE'
  AND NOT EXISTS (
    SELECT 1
    FROM branches
    WHERE branch_code = 'DASH-DEMO-BRANCH-B'
    );


INSERT INTO branches (
    branch_code,
    name_km,
    name_en,
    branch_level_id,
    parent_branch_id,
    province_id,
    status_id,
    address,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-BRANCH-C',
    'សាខាសាកល្បង គ',
    'Dashboard Demo Branch C',
    bl.id,
    parent_branch.id,
    province.id,
    branch_status.id,
    'Dashboard demonstration address C',
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branch_levels bl
         JOIN branch_statuses branch_status
              ON branch_status.code = 'ACTIVE'
         JOIN branches parent_branch
              ON parent_branch.id = (
                  SELECT MIN(id)
                  FROM branches
              )
         JOIN provinces province
              ON province.id = (
                  SELECT id
                  FROM provinces
                  ORDER BY id
                  OFFSET 2
    LIMIT 1
    )
    JOIN users admin_user
ON admin_user.id = (
    SELECT MIN(id)
    FROM users
    WHERE role = 'ADMIN'
    )
WHERE bl.code = 'PROVINCE'
  AND NOT EXISTS (
    SELECT 1
    FROM branches
    WHERE branch_code = 'DASH-DEMO-BRANCH-C'
    );


-- ============================================================
-- 2. CREATE DEMO MEMBERS
-- ============================================================

-- Branch A: two members existing before July
INSERT INTO members (
    member_no,
    full_name_km,
    full_name_en,
    branch_id,
    status_id,
    gender,
    joined_on,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-M001',
    'សមាជិកសាកល្បង ១',
    'Dashboard Demo Member 1',
    branch.id,
    member_status.id,
    'MALE',
    DATE '2026-05-10',
    admin_user.id,
    TIMESTAMPTZ '2026-05-10 08:00:00+07',
    TIMESTAMPTZ '2026-05-10 08:00:00+07'
FROM branches branch
         JOIN member_statuses member_status
              ON member_status.code = 'ACTIVE'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-A'
  AND NOT EXISTS (
    SELECT 1
    FROM members
    WHERE member_no = 'DASH-DEMO-M001'
);


INSERT INTO members (
    member_no,
    full_name_km,
    full_name_en,
    branch_id,
    status_id,
    gender,
    joined_on,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-M002',
    'សមាជិកសាកល្បង ២',
    'Dashboard Demo Member 2',
    branch.id,
    member_status.id,
    'FEMALE',
    DATE '2026-06-12',
    admin_user.id,
    TIMESTAMPTZ '2026-06-12 08:00:00+07',
    TIMESTAMPTZ '2026-06-12 08:00:00+07'
FROM branches branch
         JOIN member_statuses member_status
              ON member_status.code = 'ACTIVE'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-A'
  AND NOT EXISTS (
    SELECT 1
    FROM members
    WHERE member_no = 'DASH-DEMO-M002'
);


-- Branch A: one new July member
INSERT INTO members (
    member_no,
    full_name_km,
    full_name_en,
    branch_id,
    status_id,
    gender,
    joined_on,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-M003',
    'សមាជិកសាកល្បង ៣',
    'Dashboard Demo Member 3',
    branch.id,
    member_status.id,
    'MALE',
    DATE '2026-07-08',
    admin_user.id,
    TIMESTAMPTZ '2026-07-08 08:00:00+07',
    TIMESTAMPTZ '2026-07-08 08:00:00+07'
FROM branches branch
         JOIN member_statuses member_status
              ON member_status.code = 'ACTIVE'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-A'
  AND NOT EXISTS (
    SELECT 1
    FROM members
    WHERE member_no = 'DASH-DEMO-M003'
);


-- Branch B: one previous member
INSERT INTO members (
    member_no,
    full_name_km,
    full_name_en,
    branch_id,
    status_id,
    gender,
    joined_on,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-M004',
    'សមាជិកសាកល្បង ៤',
    'Dashboard Demo Member 4',
    branch.id,
    member_status.id,
    'FEMALE',
    DATE '2026-06-05',
    admin_user.id,
    TIMESTAMPTZ '2026-06-05 08:00:00+07',
    TIMESTAMPTZ '2026-06-05 08:00:00+07'
FROM branches branch
         JOIN member_statuses member_status
              ON member_status.code = 'ACTIVE'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-B'
  AND NOT EXISTS (
    SELECT 1
    FROM members
    WHERE member_no = 'DASH-DEMO-M004'
);


-- Branch B: one new July member
INSERT INTO members (
    member_no,
    full_name_km,
    full_name_en,
    branch_id,
    status_id,
    gender,
    joined_on,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-M005',
    'សមាជិកសាកល្បង ៥',
    'Dashboard Demo Member 5',
    branch.id,
    member_status.id,
    'MALE',
    DATE '2026-07-15',
    admin_user.id,
    TIMESTAMPTZ '2026-07-15 08:00:00+07',
    TIMESTAMPTZ '2026-07-15 08:00:00+07'
FROM branches branch
         JOIN member_statuses member_status
              ON member_status.code = 'ACTIVE'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-B'
  AND NOT EXISTS (
    SELECT 1
    FROM members
    WHERE member_no = 'DASH-DEMO-M005'
);


-- Branch C: active member
INSERT INTO members (
    member_no,
    full_name_km,
    full_name_en,
    branch_id,
    status_id,
    gender,
    joined_on,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-M006',
    'សមាជិកសាកល្បង ៦',
    'Dashboard Demo Member 6',
    branch.id,
    member_status.id,
    'FEMALE',
    DATE '2026-05-20',
    admin_user.id,
    TIMESTAMPTZ '2026-05-20 08:00:00+07',
    TIMESTAMPTZ '2026-05-20 08:00:00+07'
FROM branches branch
         JOIN member_statuses member_status
              ON member_status.code = 'ACTIVE'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-C'
  AND NOT EXISTS (
    SELECT 1
    FROM members
    WHERE member_no = 'DASH-DEMO-M006'
);


-- Branch C: inactive member, excluded from active-member cards
INSERT INTO members (
    member_no,
    full_name_km,
    full_name_en,
    branch_id,
    status_id,
    gender,
    joined_on,
    created_by,
    created_at,
    updated_at
)
SELECT
    'DASH-DEMO-M007',
    'សមាជិកអសកម្មសាកល្បង',
    'Dashboard Demo Inactive Member',
    branch.id,
    member_status.id,
    'MALE',
    DATE '2026-06-25',
    admin_user.id,
    TIMESTAMPTZ '2026-06-25 08:00:00+07',
    TIMESTAMPTZ '2026-06-25 08:00:00+07'
FROM branches branch
         JOIN member_statuses member_status
              ON member_status.code = 'INACTIVE'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-C'
  AND NOT EXISTS (
    SELECT 1
    FROM members
    WHERE member_no = 'DASH-DEMO-M007'
);


-- ============================================================
-- 3. ASSIGN SECRETARY AND BRANCH LEADER TO MULTIPLE BRANCHES
-- ============================================================

-- Secretary: Branch A
INSERT INTO branch_staff (
    branch_id,
    member_id,
    position_id,
    started_on,
    is_primary,
    appointed_by,
    created_at,
    updated_at
)
SELECT
    branch.id,
    secretary.member_id,
    COALESCE(
            (
                SELECT id
                FROM positions
                WHERE code = 'SECRETARY'
            LIMIT 1
        ),
        (
            SELECT MIN(id)
            FROM positions
        )
    ),
    DATE '2026-01-01',
    true,
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branches branch
         JOIN users secretary
              ON secretary.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'SECRETARY'
                    AND member_id IS NOT NULL
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-A'
  AND NOT EXISTS (
    SELECT 1
    FROM branch_staff bs
    WHERE bs.branch_id = branch.id
      AND bs.member_id = secretary.member_id
      AND bs.ended_on IS NULL
);


-- Secretary: Branch B
INSERT INTO branch_staff (
    branch_id,
    member_id,
    position_id,
    started_on,
    is_primary,
    appointed_by,
    created_at,
    updated_at
)
SELECT
    branch.id,
    secretary.member_id,
    COALESCE(
            (
                SELECT id
                FROM positions
                WHERE code = 'SECRETARY'
            LIMIT 1
        ),
        (
            SELECT MIN(id)
            FROM positions
        )
    ),
    DATE '2026-01-01',
    false,
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branches branch
         JOIN users secretary
              ON secretary.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'SECRETARY'
                    AND member_id IS NOT NULL
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-B'
  AND NOT EXISTS (
    SELECT 1
    FROM branch_staff bs
    WHERE bs.branch_id = branch.id
      AND bs.member_id = secretary.member_id
      AND bs.ended_on IS NULL
);


-- Branch leader: Branch B
INSERT INTO branch_staff (
    branch_id,
    member_id,
    position_id,
    started_on,
    is_primary,
    appointed_by,
    created_at,
    updated_at
)
SELECT
    branch.id,
    leader.member_id,
    COALESCE(
            (
                SELECT id
                FROM positions
                WHERE code IN (
                               'BRANCH_LEADER',
                               'LEADER'
                    )
                ORDER BY id
            LIMIT 1
        ),
        (
            SELECT MIN(id)
            FROM positions
        )
    ),
    DATE '2026-01-01',
    true,
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branches branch
         JOIN users leader
              ON leader.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'BRANCH_LEADER'
                    AND member_id IS NOT NULL
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-B'
  AND NOT EXISTS (
    SELECT 1
    FROM branch_staff bs
    WHERE bs.branch_id = branch.id
      AND bs.member_id = leader.member_id
      AND bs.ended_on IS NULL
);


-- Branch leader: Branch C
INSERT INTO branch_staff (
    branch_id,
    member_id,
    position_id,
    started_on,
    is_primary,
    appointed_by,
    created_at,
    updated_at
)
SELECT
    branch.id,
    leader.member_id,
    COALESCE(
            (
                SELECT id
                FROM positions
                WHERE code IN (
                               'BRANCH_LEADER',
                               'LEADER'
                    )
                ORDER BY id
            LIMIT 1
        ),
        (
            SELECT MIN(id)
            FROM positions
        )
    ),
    DATE '2026-01-01',
    false,
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branches branch
         JOIN users leader
              ON leader.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'BRANCH_LEADER'
                    AND member_id IS NOT NULL
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE branch.branch_code = 'DASH-DEMO-BRANCH-C'
  AND NOT EXISTS (
    SELECT 1
    FROM branch_staff bs
    WHERE bs.branch_id = branch.id
      AND bs.member_id = leader.member_id
      AND bs.ended_on IS NULL
);


-- ============================================================
-- 4. CREATE DASHBOARD ACTIVITIES
-- ============================================================

-- June completed, internal, Branch A
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
    'សកម្មភាពសាកល្បងពហុសាខា ខែមិថុនា ក',
    'Dashboard Multi Branch June A',
    'Dashboard demonstration activity',
    activity_type.id,
    activity_sector.id,
    activity_status.id,
    branch.id,
    true,
    TIMESTAMPTZ '2026-06-10 08:00:00+07',
    TIMESTAMPTZ '2026-06-10 11:00:00+07',
    branch.name_en,
    admin_user.id,
    TIMESTAMPTZ '2026-06-01 08:00:00+07',
    TIMESTAMPTZ '2026-06-01 08:00:00+07'
FROM activity_types activity_type
         JOIN activity_statuses activity_status
              ON activity_status.code = 'COMPLETED'
         JOIN activity_sectors activity_sector
              ON activity_sector.id = (
                  SELECT MIN(id)
                  FROM activity_sectors
              )
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-A'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE activity_type.code = 'INTERNAL'
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Multi Branch June A'
);


-- June completed, external, Branch B
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
    'សកម្មភាពសាកល្បងពហុសាខា ខែមិថុនា ខ',
    'Dashboard Multi Branch June B',
    'Dashboard demonstration activity',
    activity_type.id,
    activity_sector.id,
    activity_status.id,
    branch.id,
    true,
    TIMESTAMPTZ '2026-06-20 09:00:00+07',
    TIMESTAMPTZ '2026-06-20 12:00:00+07',
    branch.name_en,
    admin_user.id,
    TIMESTAMPTZ '2026-06-05 08:00:00+07',
    TIMESTAMPTZ '2026-06-05 08:00:00+07'
FROM activity_types activity_type
         JOIN activity_statuses activity_status
              ON activity_status.code = 'COMPLETED'
         JOIN activity_sectors activity_sector
              ON activity_sector.id = (
                  SELECT MIN(id)
                  FROM activity_sectors
              )
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-B'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE activity_type.code = 'EXTERNAL'
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Multi Branch June B'
);


-- July completed, internal, Branch A
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
    'សកម្មភាពសាកល្បងពហុសាខា ខែកក្កដា ក',
    'Dashboard Multi Branch July A',
    'Dashboard demonstration activity',
    activity_type.id,
    activity_sector.id,
    activity_status.id,
    branch.id,
    true,
    TIMESTAMPTZ '2026-07-12 08:00:00+07',
    TIMESTAMPTZ '2026-07-12 11:00:00+07',
    branch.name_en,
    admin_user.id,
    TIMESTAMPTZ '2026-07-01 08:00:00+07',
    TIMESTAMPTZ '2026-07-01 08:00:00+07'
FROM activity_types activity_type
         JOIN activity_statuses activity_status
              ON activity_status.code = 'COMPLETED'
         JOIN activity_sectors activity_sector
              ON activity_sector.id = (
                  SELECT MIN(id)
                  FROM activity_sectors
              )
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-A'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE activity_type.code = 'INTERNAL'
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Multi Branch July A'
);


-- July completed, external, Branch B
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
    'សកម្មភាពសាកល្បងពហុសាខា ខែកក្កដា ខ',
    'Dashboard Multi Branch July B',
    'Dashboard demonstration activity',
    activity_type.id,
    activity_sector.id,
    activity_status.id,
    branch.id,
    true,
    TIMESTAMPTZ '2026-07-22 09:00:00+07',
    TIMESTAMPTZ '2026-07-22 12:00:00+07',
    branch.name_en,
    admin_user.id,
    TIMESTAMPTZ '2026-07-05 08:00:00+07',
    TIMESTAMPTZ '2026-07-05 08:00:00+07'
FROM activity_types activity_type
         JOIN activity_statuses activity_status
              ON activity_status.code = 'COMPLETED'
         JOIN activity_sectors activity_sector
              ON activity_sector.id = (
                  SELECT MIN(id)
                  FROM activity_sectors
              )
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-B'
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE activity_type.code = 'EXTERNAL'
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Multi Branch July B'
);


-- Future upcoming activity, Branch B
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
    'សកម្មភាពពហុសាខានាពេលខាងមុខ ខ',
    'Dashboard Multi Branch Upcoming B',
    'Future dashboard demonstration activity',
    activity_type.id,
    activity_sector.id,
    activity_status.id,
    branch.id,
    true,
    CURRENT_TIMESTAMP + INTERVAL '10 days',
    CURRENT_TIMESTAMP + INTERVAL '10 days 3 hours',
    branch.name_en,
    admin_user.id,
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
    JOIN branches branch
    ON branch.branch_code = 'DASH-DEMO-BRANCH-B'
    JOIN users admin_user
    ON admin_user.id = (
    SELECT MIN(id)
    FROM users
    WHERE role = 'ADMIN'
    )
WHERE activity_type.code = 'INTERNAL'
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Multi Branch Upcoming B'
    );


-- Future upcoming activity, Branch C
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
    'សកម្មភាពពហុសាខានាពេលខាងមុខ គ',
    'Dashboard Multi Branch Upcoming C',
    'Future dashboard demonstration activity',
    activity_type.id,
    activity_sector.id,
    activity_status.id,
    branch.id,
    true,
    CURRENT_TIMESTAMP + INTERVAL '20 days',
    CURRENT_TIMESTAMP + INTERVAL '20 days 3 hours',
    branch.name_en,
    admin_user.id,
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
    JOIN branches branch
    ON branch.branch_code = 'DASH-DEMO-BRANCH-C'
    JOIN users admin_user
    ON admin_user.id = (
    SELECT MIN(id)
    FROM users
    WHERE role = 'ADMIN'
    )
WHERE activity_type.code = 'EXTERNAL'
  AND NOT EXISTS (
    SELECT 1
    FROM activities
    WHERE title_en = 'Dashboard Multi Branch Upcoming C'
    );


-- ============================================================
-- 5. CREATE ACTIVITY PARTICIPANTS
-- ============================================================

-- Add Branch A members to Branch A activities.
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
    activity.id,
    member.id,
    activity.starts_at - INTERVAL '5 days',
    admin_user.id,
    'MANUAL',
    activity.starts_at - INTERVAL '5 days',
    activity.starts_at - INTERVAL '5 days'
FROM activities activity
    JOIN members member
ON member.member_no IN (
    'DASH-DEMO-M001',
    'DASH-DEMO-M002',
    'DASH-DEMO-M003'
    )
    JOIN users admin_user
    ON admin_user.id = (
    SELECT MIN(id)
    FROM users
    WHERE role = 'ADMIN'
    )
WHERE activity.title_en IN (
    'Dashboard Multi Branch June A',
    'Dashboard Multi Branch July A'
    )
ON CONFLICT (activity_id, member_id)
    DO NOTHING;


-- Add Branch B members to Branch B activities.
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
    activity.id,
    member.id,
    activity.starts_at - INTERVAL '5 days',
    admin_user.id,
    'MANUAL',
    activity.starts_at - INTERVAL '5 days',
    activity.starts_at - INTERVAL '5 days'
FROM activities activity
    JOIN members member
ON member.member_no IN (
    'DASH-DEMO-M004',
    'DASH-DEMO-M005'
    )
    JOIN users admin_user
    ON admin_user.id = (
    SELECT MIN(id)
    FROM users
    WHERE role = 'ADMIN'
    )
WHERE activity.title_en IN (
    'Dashboard Multi Branch June B',
    'Dashboard Multi Branch July B',
    'Dashboard Multi Branch Upcoming B'
    )
ON CONFLICT (activity_id, member_id)
    DO NOTHING;


-- Add Branch C member to Branch C upcoming activity.
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
    activity.id,
    member.id,
    activity.starts_at - INTERVAL '5 days',
    admin_user.id,
    'MANUAL',
    activity.starts_at - INTERVAL '5 days',
    activity.starts_at - INTERVAL '5 days'
FROM activities activity
    JOIN members member
ON member.member_no = 'DASH-DEMO-M006'
    JOIN users admin_user
    ON admin_user.id = (
    SELECT MIN(id)
    FROM users
    WHERE role = 'ADMIN'
    )
WHERE activity.title_en =
    'Dashboard Multi Branch Upcoming C'
ON CONFLICT (activity_id, member_id)
    DO NOTHING;


-- ============================================================
-- 6. CREATE DASHBOARD DONATIONS
-- ============================================================

-- June KHR donation, Branch A
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
    'DASH-DEMO-DON-001',
    donation_type.id,
    'Dashboard Demo Donor A',
    branch.id,
    DATE '2026-06-01',
    400000,
    0,
    4000,
    100,
    payment_method.id,
    TIMESTAMPTZ '2026-06-10 10:00:00+07',
    admin_user.id,
    'Dashboard June KHR donation',
    TIMESTAMPTZ '2026-06-10 10:00:00+07',
    TIMESTAMPTZ '2026-06-10 10:00:00+07'
FROM donation_types donation_type
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-A'
         JOIN payment_methods payment_method
              ON payment_method.id = (
                  SELECT MIN(id)
                  FROM payment_methods
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE donation_type.code = 'MONTHLY_DONATION'
  AND NOT EXISTS (
    SELECT 1
    FROM donations
    WHERE donation_no = 'DASH-DEMO-DON-001'
);


-- June USD donation, Branch B
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
    'DASH-DEMO-DON-002',
    donation_type.id,
    'Dashboard Demo Donor B',
    branch.id,
    DATE '2026-06-01',
    0,
    100,
    4000,
    100,
    payment_method.id,
    TIMESTAMPTZ '2026-06-18 10:00:00+07',
    admin_user.id,
    'Dashboard June USD donation',
    TIMESTAMPTZ '2026-06-18 10:00:00+07',
    TIMESTAMPTZ '2026-06-18 10:00:00+07'
FROM donation_types donation_type
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-B'
         JOIN payment_methods payment_method
              ON payment_method.id = (
                  SELECT MIN(id)
                  FROM payment_methods
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE donation_type.code = 'SPONSOR_DONATION'
  AND NOT EXISTS (
    SELECT 1
    FROM donations
    WHERE donation_no = 'DASH-DEMO-DON-002'
);


-- July KHR donation, Branch A
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
    'DASH-DEMO-DON-003',
    donation_type.id,
    'Dashboard Demo Donor C',
    branch.id,
    DATE '2026-07-01',
    600000,
    0,
    4000,
    150,
    payment_method.id,
    TIMESTAMPTZ '2026-07-10 10:00:00+07',
    admin_user.id,
    'Dashboard July KHR donation',
    TIMESTAMPTZ '2026-07-10 10:00:00+07',
    TIMESTAMPTZ '2026-07-10 10:00:00+07'
FROM donation_types donation_type
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-A'
         JOIN payment_methods payment_method
              ON payment_method.id = (
                  SELECT MIN(id)
                  FROM payment_methods
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE donation_type.code = 'MONTHLY_DONATION'
  AND NOT EXISTS (
    SELECT 1
    FROM donations
    WHERE donation_no = 'DASH-DEMO-DON-003'
);


-- July USD donation, Branch B
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
    'DASH-DEMO-DON-004',
    donation_type.id,
    'Dashboard Demo Donor D',
    branch.id,
    DATE '2026-07-01',
    0,
    150,
    4000,
    150,
    payment_method.id,
    TIMESTAMPTZ '2026-07-20 10:00:00+07',
    admin_user.id,
    'Dashboard July USD donation',
    TIMESTAMPTZ '2026-07-20 10:00:00+07',
    TIMESTAMPTZ '2026-07-20 10:00:00+07'
FROM donation_types donation_type
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-B'
         JOIN payment_methods payment_method
              ON payment_method.id = (
                  SELECT MIN(id)
                  FROM payment_methods
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE donation_type.code = 'SPONSOR_DONATION'
  AND NOT EXISTS (
    SELECT 1
    FROM donations
    WHERE donation_no = 'DASH-DEMO-DON-004'
);


-- July mixed donation, Branch C
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
    'DASH-DEMO-DON-005',
    donation_type.id,
    'Dashboard Demo Donor E',
    branch.id,
    DATE '2026-07-01',
    200000,
    50,
    4000,
    100,
    payment_method.id,
    TIMESTAMPTZ '2026-07-25 10:00:00+07',
    admin_user.id,
    'Dashboard July mixed donation',
    TIMESTAMPTZ '2026-07-25 10:00:00+07',
    TIMESTAMPTZ '2026-07-25 10:00:00+07'
FROM donation_types donation_type
         JOIN branches branch
              ON branch.branch_code = 'DASH-DEMO-BRANCH-C'
         JOIN payment_methods payment_method
              ON payment_method.id = (
                  SELECT MIN(id)
                  FROM payment_methods
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE donation_type.code = 'ACTIVITY_DONATION'
  AND NOT EXISTS (
    SELECT 1
    FROM donations
    WHERE donation_no = 'DASH-DEMO-DON-005'
);


-- ============================================================
-- END OF DASHBOARD MULTI-BRANCH SEED
-- ============================================================