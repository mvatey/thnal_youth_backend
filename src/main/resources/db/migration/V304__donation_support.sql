-- V23: application support for the donation module.
--
-- V8 created the `donations` table but left two things to the application:
--   1. donation_no is NOT NULL UNIQUE with no default  -> the app must mint it.
--   2. no idempotency guard for double-submit / retry on create.
--
-- This migration adds both, backward-compatibly. It does NOT change any V8
-- column or constraint (V1-V22 are immutable once applied).

-- ------------------------------------------------------------
-- 1. Donation-number sequence
-- ------------------------------------------------------------
-- A single global sequence is enough for uniqueness. The service formats the
-- human-facing number as  DON-{yyyyMMdd}-{seq padded to 6}  (e.g.
-- DON-20260724-000042). nextval() is atomic, so concurrent creates never
-- collide on donation_no even under load.
CREATE SEQUENCE IF NOT EXISTS donation_no_seq
    AS BIGINT
    START WITH 1
    INCREMENT BY 1
    NO CYCLE;

-- ------------------------------------------------------------
-- 2. Idempotency key (mirrors V22 for notifications)
-- ------------------------------------------------------------
-- Lets a client pass a UUID so a double-submit / retry from the same recorder
-- collapses to a single donation instead of inserting twice.
--
-- Backward compatible: the column is nullable and uniqueness is a PARTIAL index
-- (only rows WHERE client_request_id IS NOT NULL participate), so existing
-- callers that omit the key are unaffected.
ALTER TABLE donations
    ADD COLUMN IF NOT EXISTS client_request_id UUID;

-- One logical create per (recorder, client_request_id). Scoped to recorded_by
-- so two different staff reusing the same UUID never collide.
CREATE UNIQUE INDEX IF NOT EXISTS uq_donations_recorder_client_request
    ON donations (recorded_by, client_request_id)
    WHERE client_request_id IS NOT NULL;
