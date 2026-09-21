-- One-time backfill: clear members.phone/email for every member whose
-- linked login was already disabled (status = INACTIVE) before this fix
-- shipped, matching the app-level behavior now applied on every future
-- disable (see MemberPasswordServiceImpl.disableAccount). Without this,
-- an already-disabled member's original phone/email stay permanently
-- unique-constrained against reuse by anyone else.
UPDATE members m
SET phone = NULL,
    email = NULL
FROM users u
WHERE u.member_id = m.id
  AND u.status = 'INACTIVE'
  AND (m.phone IS NOT NULL OR m.email IS NOT NULL);
