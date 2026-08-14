-- ============================================================
-- V329 - Add VIEWER role
--
-- Purpose:
--   Introduce a read-only "VIEWER" account role. VIEWER accounts
--   are not linked to a branch/member (users.member_id stays NULL)
--   and are created directly by an ADMIN through the new
--   /api/admin/users management endpoints.
--
--   VIEWER has the same viewing authority ADMIN has across the
--   modules already exposed to ADMIN, but is never permitted to
--   create, update, or delete anything. That write restriction is
--   enforced entirely at the application layer (VIEWER is never
--   added to a mutating endpoint's allowed-roles list) — this
--   migration only widens the users.role CHECK constraint so the
--   value can be persisted.
-- ============================================================

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_role;

ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (
            role IN (
                     'ADMIN',
                     'BRANCH_LEADER',
                     'SECRETARY',
                     'MEMBER',
                     'VIEWER'
                )
            );
