-- One-time backfill: free up username/phone/email on accounts that were
-- already soft-deleted (status = INACTIVE) before this fix shipped, so
-- those values become reusable too -- matching the app-level behavior now
-- applied to every future delete/disable (see User.freeIdentifiersForReuse).
UPDATE users
SET username = 'deleted_user_' || id,
    phone = NULL,
    email = NULL
WHERE status = 'INACTIVE';
