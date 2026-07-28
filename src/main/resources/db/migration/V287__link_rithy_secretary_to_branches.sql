-- ============================================================
-- LINK RITHY SECRETARY ACCOUNT TO MEMBER AND BRANCHES
--
-- Target account:
--   users.id = 3
--   users.email = rithyphan@gmail.com
--
-- This migration:
--   1. Verifies the secretary account exists
--   2. Creates a member record when necessary
--   3. Links users.member_id
--   4. Assigns the member to up to two active branches
-- ============================================================

DO $$
DECLARE
target_user_id BIGINT;
    target_member_id BIGINT;
    default_branch_id BIGINT;
    secretary_position_id SMALLINT;
    active_member_status_id BIGINT;
BEGIN
    -- --------------------------------------------------------
    -- 1. Find the exact secretary user
    -- --------------------------------------------------------
SELECT u.id
INTO target_user_id
FROM users u
WHERE u.id = 1
  AND LOWER(u.email) =
      LOWER('rithyphan@gmail.com')
  AND u.role = 'SECRETARY';

IF target_user_id IS NULL THEN
        RAISE EXCEPTION
            'Secretary user id=3 with email rithyphan@gmail.com was not found';
END IF;

    -- --------------------------------------------------------
    -- 2. Find the first active branch
    -- --------------------------------------------------------
SELECT b.id
INTO default_branch_id
FROM branches b
         JOIN branch_statuses bs
              ON bs.id = b.status_id
WHERE bs.code = 'ACTIVE'
ORDER BY b.id
    LIMIT 1;

IF default_branch_id IS NULL THEN
        RAISE EXCEPTION
            'No active branch exists for the secretary assignment';
END IF;

    -- --------------------------------------------------------
    -- 3. Resolve ACTIVE member status
    -- --------------------------------------------------------
SELECT ms.id
INTO active_member_status_id
FROM member_statuses ms
WHERE ms.code = 'ACTIVE'
ORDER BY ms.id
    LIMIT 1;

IF active_member_status_id IS NULL THEN
        RAISE EXCEPTION
            'ACTIVE member status was not found';
END IF;

    -- --------------------------------------------------------
    -- 4. Reuse an existing member when possible
    -- --------------------------------------------------------
SELECT u.member_id
INTO target_member_id
FROM users u
WHERE u.id = target_user_id;

IF target_member_id IS NULL THEN
SELECT m.id
INTO target_member_id
FROM members m
WHERE LOWER(m.email) =
      LOWER('rithyphan@gmail.com')
   OR m.phone = (
    SELECT u.phone
    FROM users u
    WHERE u.id = target_user_id
)
ORDER BY m.id
    LIMIT 1;
END IF;

    -- --------------------------------------------------------
    -- 5. Create a member when none exists
    -- --------------------------------------------------------
    IF target_member_id IS NULL THEN
        INSERT INTO members (
            member_no,
            full_name_km,
            full_name_en,
            branch_id,
            status_id,
            gender,
            phone,
            email,
            joined_on,
            created_by,
            created_at,
            updated_at
        )
SELECT
    'DASH-STAFF-RITHY-003',
    COALESCE(
            NULLIF(u.full_name_km, ''),
            'រិទ្ធី ផាន់'
    ),
    COALESCE(
            NULLIF(u.full_name_en, ''),
            'Rithy Phan'
    ),
    default_branch_id,
    active_member_status_id,
    'MALE',
    u.phone,
    u.email,
    CURRENT_DATE,
    target_user_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u
WHERE u.id = target_user_id
    RETURNING id
INTO target_member_id;
END IF;

    -- --------------------------------------------------------
    -- 6. Link the user account to the member
    -- --------------------------------------------------------
UPDATE users
SET
    member_id = target_member_id,
    updated_at = CURRENT_TIMESTAMP
WHERE id = target_user_id
  AND member_id IS DISTINCT FROM target_member_id;

-- --------------------------------------------------------
-- 7. Resolve secretary position
-- --------------------------------------------------------
SELECT p.id
INTO secretary_position_id
FROM positions p
WHERE p.code = 'SECRETARY'
ORDER BY p.id
    LIMIT 1;

-- Fallback if the exact position code is absent.
IF secretary_position_id IS NULL THEN
SELECT MIN(p.id)
INTO secretary_position_id
FROM positions p;
END IF;

    IF secretary_position_id IS NULL THEN
        RAISE EXCEPTION
            'No position exists for branch_staff assignment';
END IF;

    -- --------------------------------------------------------
    -- 8. Assign the first active branch as primary
    -- --------------------------------------------------------
INSERT INTO branch_staff (
    branch_id,
    member_id,
    position_id,
    started_on,
    ended_on,
    is_primary,
    appointed_by,
    created_at,
    updated_at
)
VALUES (
           default_branch_id,
           target_member_id,
           secretary_position_id,
           CURRENT_DATE,
           NULL,
           true,
           target_user_id,
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       )
    ON CONFLICT DO NOTHING;

-- --------------------------------------------------------
-- 9. Assign a second active branch when available
-- --------------------------------------------------------
INSERT INTO branch_staff (
    branch_id,
    member_id,
    position_id,
    started_on,
    ended_on,
    is_primary,
    appointed_by,
    created_at,
    updated_at
)
SELECT
    b.id,
    target_member_id,
    secretary_position_id,
    CURRENT_DATE,
    NULL,
    false,
    target_user_id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branches b
         JOIN branch_statuses bs
              ON bs.id = b.status_id
WHERE bs.code = 'ACTIVE'
  AND b.id <> default_branch_id
  AND NOT EXISTS (
    SELECT 1
    FROM branch_staff existing
    WHERE existing.branch_id = b.id
      AND existing.member_id =
          target_member_id
      AND existing.position_id =
          secretary_position_id
      AND existing.ended_on IS NULL
)
ORDER BY b.id
    LIMIT 1;
END
$$;