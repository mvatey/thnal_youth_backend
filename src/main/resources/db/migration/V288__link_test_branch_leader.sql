DO $$
DECLARE
v_leader_user_id BIGINT;
    v_leader_member_id BIGINT;
    v_leader_branch_id BIGINT;
    v_leader_position_id SMALLINT := 1;
    v_active_status_id SMALLINT;
    v_admin_user_id BIGINT;
BEGIN
    /*
     * Select an existing branch.
     * No branch code column is required.
     */
SELECT b.id
INTO v_leader_branch_id
FROM branches b
ORDER BY b.id
    LIMIT 1;

IF v_leader_branch_id IS NULL THEN
        RAISE EXCEPTION
            'No branch exists. Create a branch before assigning a branch leader.';
END IF;

    /*
     * Find the BRANCH_LEADER user account.
     */
SELECT u.id
INTO v_leader_user_id
FROM users u
WHERE LOWER(u.email) = LOWER('leader1@gmail.com')
  AND UPPER(CAST(u.role AS TEXT)) = 'BRANCH_LEADER'
    LIMIT 1;

IF v_leader_user_id IS NULL THEN
        RAISE EXCEPTION
            'Branch leader account leader1@gmail.com was not found';
END IF;

    /*
     * Find ACTIVE member status.
     */
SELECT ms.id
INTO v_active_status_id
FROM member_statuses ms
WHERE UPPER(ms.code) = 'ACTIVE'
    LIMIT 1;

IF v_active_status_id IS NULL THEN
        RAISE EXCEPTION
            'ACTIVE member status was not found';
END IF;

    /*
     * Check whether the member already exists.
     */
SELECT m.id
INTO v_leader_member_id
FROM members m
WHERE LOWER(m.email) = LOWER('leader1@gmail.com')
    LIMIT 1;

/*
 * Create the member if missing.
 */
IF v_leader_member_id IS NULL THEN
        INSERT INTO members (
            member_no,
            full_name_km,
            full_name_en,
            branch_id,
            status_id,
            gender,
            phone,
            email,
            joined_on
        )
        VALUES (
            'TEST-LEADER-001',
            'ប្រធានសាខាសាកល្បង',
            'Test Branch Leader',
            v_leader_branch_id,
            v_active_status_id,
            'MALE',
            '010000003',
            'leader1@gmail.com',
            CURRENT_DATE
        )
        RETURNING id INTO v_leader_member_id;
ELSE
UPDATE members
SET branch_id = v_leader_branch_id,
    status_id = v_active_status_id,
    updated_at = CURRENT_TIMESTAMP
WHERE id = v_leader_member_id;
END IF;

    /*
     * Find an admin who performs the appointment.
     */
SELECT u.id
INTO v_admin_user_id
FROM users u
WHERE UPPER(CAST(u.role AS TEXT)) = 'ADMIN'
ORDER BY u.id
    LIMIT 1;

IF v_admin_user_id IS NULL THEN
        RAISE EXCEPTION
            'An ADMIN account is required to appoint the branch leader';
END IF;

    /*
     * Connect the user account to the member.
     */
UPDATE users
SET member_id = v_leader_member_id,
    updated_at = CURRENT_TIMESTAMP
WHERE id = v_leader_user_id
  AND member_id IS DISTINCT FROM v_leader_member_id;

/*
 * End another active primary leader assignment for this branch.
 */
UPDATE branch_staff
SET ended_on = CURRENT_DATE,
    is_primary = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE branch_id = v_leader_branch_id
  AND position_id = v_leader_position_id
  AND is_primary = TRUE
  AND ended_on IS NULL
  AND member_id <> v_leader_member_id;

/*
 * Insert the assignment only when it does not already exist.
 */
IF NOT EXISTS (
        SELECT 1
        FROM branch_staff bs
        WHERE bs.branch_id = v_leader_branch_id
          AND bs.member_id = v_leader_member_id
          AND bs.position_id = v_leader_position_id
          AND bs.ended_on IS NULL
    ) THEN
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
            v_leader_branch_id,
            v_leader_member_id,
            v_leader_position_id,
            CURRENT_DATE,
            NULL,
            TRUE,
            v_admin_user_id,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
ELSE
UPDATE branch_staff
SET is_primary = TRUE,
    appointed_by = v_admin_user_id,
    updated_at = CURRENT_TIMESTAMP
WHERE branch_id = v_leader_branch_id
  AND member_id = v_leader_member_id
  AND position_id = v_leader_position_id
  AND ended_on IS NULL;
END IF;
END
$$;