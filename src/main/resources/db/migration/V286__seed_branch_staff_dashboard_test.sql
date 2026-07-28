-- Assign the first secretary with a linked member
-- to two demo branches.

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
    secretary.member_id,
    p.id,
    DATE '2026-01-01',
    NULL,
    true,
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branches b
         JOIN users secretary
              ON secretary.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'SECRETARY'
                    AND member_id IS NOT NULL
              )
         JOIN positions p
              ON p.id = (
                  SELECT MIN(id)
                  FROM positions
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE b.branch_code = 'DASH-DEMO-BRANCH-A'
  AND NOT EXISTS (
    SELECT 1
    FROM branch_staff bs
    WHERE bs.branch_id = b.id
      AND bs.member_id = secretary.member_id
      AND bs.position_id = p.id
      AND bs.ended_on IS NULL
);

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
    secretary.member_id,
    p.id,
    DATE '2026-01-01',
    NULL,
    false,
    admin_user.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM branches b
         JOIN users secretary
              ON secretary.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'SECRETARY'
                    AND member_id IS NOT NULL
              )
         JOIN positions p
              ON p.id = (
                  SELECT MIN(id)
                  FROM positions
              )
         JOIN users admin_user
              ON admin_user.id = (
                  SELECT MIN(id)
                  FROM users
                  WHERE role = 'ADMIN'
              )
WHERE b.branch_code = 'DASH-DEMO-BRANCH-B'
  AND NOT EXISTS (
    SELECT 1
    FROM branch_staff bs
    WHERE bs.branch_id = b.id
      AND bs.member_id = secretary.member_id
      AND bs.position_id = p.id
      AND bs.ended_on IS NULL
);