-- Backfill: an activity with no cover_image_id but at least one gallery
-- photo already uploaded gets its earliest (lowest sort_order) photo set
-- as the cover image now.
--
-- This fixes activities that got permanently stuck with no cover image
-- because of a bug in ActivityMediaServiceImpl.uploadGalleryImages: the
-- auto-assign-cover logic only ever fired on the very first gallery
-- upload attempt for an activity (nextSortOrder == 0). If that first
-- attempt didn't include a usable image, every later upload just added
-- another gallery photo and cover_image_id stayed null forever, even
-- though photos existed. See the corresponding code fix, which removes
-- that restriction so future uploads self-heal — this migration repairs
-- the activities that were already stuck before that fix shipped.
UPDATE activities a
SET cover_image_id = sub.file_id
FROM (
    SELECT DISTINCT ON (activity_id)
        activity_id,
        file_id
    FROM activity_photos
    ORDER BY activity_id, sort_order ASC, id ASC
) sub
WHERE a.id = sub.activity_id
  AND a.cover_image_id IS NULL;
