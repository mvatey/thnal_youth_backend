-- ============================================================
-- V334 - Keep linked user branch scope aligned with member branch
--
-- Phase 1 user/member architecture:
--   * users.member_id IS NULL  -> standalone login account
--   * users.member_id IS NOT NULL -> login linked to a member
--
-- For linked accounts, users.branch_id mirrors members.branch_id so
-- branch-scoped queries that read users.branch_id remain correct.
-- Future member updates are synchronized by MemberServiceImpl.
-- ============================================================

UPDATE users u
SET branch_id = m.branch_id,
    updated_at = NOW()
FROM members m
WHERE u.member_id = m.id
  AND u.branch_id IS DISTINCT FROM m.branch_id;
