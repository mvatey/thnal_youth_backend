-- Adds optional English title/body alongside the existing Khmer-only
-- notifications.title/body (V11), so the in-app notification list can show
-- English content when the viewer's UI language is English. Nullable and
-- backfill-free: existing rows simply have no English variant and fall back
-- to the Khmer text on the frontend, exactly like every other bilingual
-- lookup in this schema (e.g. branches.name_en, activities.title_en).
ALTER TABLE notifications
    ADD COLUMN title_en VARCHAR(200),
    ADD COLUMN body_en  VARCHAR(4000);
