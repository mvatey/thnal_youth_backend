DO $$
    DECLARE
        leader_user_id BIGINT;
        leader_member_id BIGINT;
        leader_branch_id BIGINT := 73;
        leader_position_id SMALLINT := 1;
        active_status_id SMALLINT;
        admin_user_id BIGINT;
    BEGIN
        -- Find the BRANCH_LEADER user account
        SELECT id
        INTO leader_user_id
        FROM users
        WHERE LOWER(email) = LOWER('leader1@gmail.com')
          AND role = 'BRANCH_LEADER'
        LIMIT 1;

        IF leader_user_id IS NULL THEN
            RAISE EXCEPTION
                'Branch leader account leader1@gmail.com was not found';
        END IF;

        -- Find ACTIVE member status
        SELECT id
        INTO active_status_id
        FROM member_statuses
        WHERE UPPER(code) = 'ACTIVE'
        LIMIT 1;

        IF active_status_id IS NULL THEN
            RAISE EXCEPTION
                'ACTIVE member status was not found';
        END IF;

        -- Check whether the member already exists
        SELECT id
        INTO leader_member_id
        FROM members
        WHERE LOWER(email) = LOWER('leader1@gmail.com')
        LIMIT 1;

        -- Create the member when missing
        IF leader_member_id IS NULL THEN
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
                       leader_branch_id,
                       active_status_id,
                       'MALE',
                       '010000003',
                       'leader1@gmail.com',
                       CURRENT_DATE
                   )
            RETURNING id INTO leader_member_id;
        ELSE
            -- Make sure the existing member belongs to branch 3
            UPDATE members
            SET
                branch_id = leader_branch_id,
                status_id = active_status_id,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = leader_member_id;
        END IF;

        -- Find an admin who appointed the branch leader
        SELECT id
        INTO admin_user_id
        FROM users
        WHERE role = 'ADMIN'
        ORDER BY id
        LIMIT 1;

        -- Link users.member_id to the created/found member
        UPDATE users
        SET
            member_id = leader_member_id,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = leader_user_id
          AND member_id IS DISTINCT FROM leader_member_id;

        -- Add the member as branch staff
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
                   leader_branch_id,
                   leader_member_id,
                   leader_position_id,
                   CURRENT_DATE,
                   NULL,
                   TRUE,
                   admin_user_id,
                   CURRENT_TIMESTAMP,
                   CURRENT_TIMESTAMP
               )
        ON CONFLICT DO NOTHING;
    END
$$;