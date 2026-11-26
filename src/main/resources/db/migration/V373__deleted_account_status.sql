-- A login account can now be DELETED, separate from INACTIVE (disabled).
-- Until now both were INACTIVE, so a member whose login was switched off
-- (made inactive, resigned, fired, deceased) dropped out of the member list
-- and every count, exactly like a deleted one. From now on only DELETED
-- hides a member; INACTIVE members are listed and counted.

ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_status;

ALTER TABLE users
    ADD CONSTRAINT chk_users_status
        CHECK (status IN ('PENDING_ACTIVATION', 'ACTIVE', 'INACTIVE', 'LOCKED', 'DELETED'));

-- Members who are gone for good stay hidden: standalone accounts, members who
-- resigned or were fired, and members who are otherwise active and alive
-- (their login was only deleted) all become DELETED. Only members who are
-- inactive or deceased keep an INACTIVE login, so they are listed and counted.
UPDATE users u
SET status = 'DELETED'
WHERE u.status = 'INACTIVE'
  AND (
        u.member_id IS NULL
        OR NOT EXISTS (
            SELECT 1
            FROM members m
            LEFT JOIN member_statuses s ON s.id = m.status_id
            WHERE m.id = u.member_id
              AND UPPER(COALESCE(s.code, '')) NOT IN ('RESIGNED', 'FIRED')
              AND (
                    UPPER(COALESCE(s.code, '')) = 'INACTIVE'
                    OR UPPER(m.life_status) = 'DECEASED'
              )
        )
      );
