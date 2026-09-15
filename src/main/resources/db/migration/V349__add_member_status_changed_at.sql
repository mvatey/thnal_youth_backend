-- Tracks when a member's status_id last actually changed, distinct from
-- updated_at (which bumps on any field edit). Needed so screens that add a
-- member to something new (an activity, a document) can tell whether that
-- thing was created before or after the member went inactive, and only
-- block the newer case.
ALTER TABLE members
    ADD COLUMN status_changed_at TIMESTAMPTZ;

-- Best-effort backfill for existing rows: updated_at is the closest proxy
-- we have for "the last time this row was touched," which for most rows
-- is also the last status change. Not exact, but a reasonable default
-- that never overstates how recently someone went inactive.
UPDATE members
SET status_changed_at = updated_at
WHERE status_changed_at IS NULL;

ALTER TABLE members
    ALTER COLUMN status_changed_at SET NOT NULL;
