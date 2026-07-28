DO $$
DECLARE
leader_user_id BIGINT;
    leader_member_id BIGINT := 7;
    leader_branch_id BIGINT := 3;
    leader_position_id SMALLINT := 1;
    admin_user_id BIGINT;
BEGIN
SELECT id
INTO leader_user_id
FROM users
WHERE LOWER(email) =
      LOWER('leader1@gmail.com')
  AND role = 'BRANCH_LEADER';

IF leader_user_id IS NULL THEN
        RAISE EXCEPTION
            'Branch leader account leader1@gmail.com was not found';
END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM members
        WHERE id = leader_member_id
          AND branch_id = leader_branch_id
    ) THEN
        RAISE EXCEPTION
            'Member 7 does not belong to branch 3';
END IF;

SELECT id
INTO admin_user_id
FROM users
WHERE role = 'ADMIN'
ORDER BY id
    LIMIT 1;

UPDATE users
SET
    member_id = leader_member_id,
    updated_at = CURRENT_TIMESTAMP
WHERE id = leader_user_id
  AND member_id IS DISTINCT FROM leader_member_id;

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
           true,
           admin_user_id,
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       )
    ON CONFLICT DO NOTHING;
END
$$;