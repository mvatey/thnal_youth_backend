-- One-time backfill: free up telegram_chat_id on accounts that were already
-- soft-deleted (status = INACTIVE) before this fix shipped, so that Telegram
-- account becomes reconnectable to a future new account too -- matching the
-- app-level behavior now applied to every future delete/disable (see
-- User.freeIdentifiersForReuse). The unique constraint on telegram_chat_id
-- meant a deleted account's linked Telegram was permanently stuck, blocking
-- even the same person from reconnecting it on a replacement account.
UPDATE users
SET telegram_chat_id = NULL,
    telegram_linked_at = NULL
WHERE status = 'INACTIVE'
  AND telegram_chat_id IS NOT NULL;
