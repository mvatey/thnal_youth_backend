-- Phase 3 authorization invariant:
-- a staff member may have only one active PRIMARY position across branches.
-- SECRETARY may still cover many additional branches because those rows are non-primary.
-- BRANCH_LEADER therefore cannot lead two branches at the same time.

WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY member_id
               ORDER BY
                   CASE WHEN branch_id = (SELECT branch_id FROM members m WHERE m.id = branch_staff.member_id)
                        THEN 0 ELSE 1 END,
                   started_on ASC,
                   id ASC
           ) AS rn
    FROM branch_staff
    WHERE ended_on IS NULL
      AND is_primary = TRUE
)
UPDATE branch_staff bs
SET is_primary = FALSE,
    updated_at = NOW()
FROM ranked r
WHERE bs.id = r.id
  AND r.rn > 1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_branch_staff_member_single_primary
    ON branch_staff(member_id)
    WHERE ended_on IS NULL
      AND is_primary = TRUE;
