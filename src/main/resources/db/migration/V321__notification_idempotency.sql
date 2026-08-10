-- V22: idempotency key for notification creation.
--
-- (Renamed from V21 -> V22 to resolve a Flyway version collision with
--  V21__upsert_temporary_role_accounts.sql. Content is unchanged.)
--
-- Lets a client pass a UUID so a double-submit / retry from the same admin
-- collapses to a single notification instead of fanning out twice.
--
-- Backward compatible: the column is nullable and the uniqueness rule is a
-- PARTIAL index (only rows WHERE client_request_id IS NOT NULL participate),
-- so existing callers that omit the key are unaffected and can create as many
-- notifications as before.

ALTER TABLE notifications
    ADD COLUMN client_request_id UUID;

-- One logical create per (creator, client_request_id). Scoped to created_by so
-- two different admins reusing the same UUID never collide.
CREATE UNIQUE INDEX uq_notifications_creator_client_request
    ON notifications (created_by, client_request_id)
    WHERE client_request_id IS NOT NULL;
