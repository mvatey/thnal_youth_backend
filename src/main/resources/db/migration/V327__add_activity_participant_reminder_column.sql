-- Tracks whether the "1 day before the event" reminder notification has
-- already been sent to a given activity participant, so the scheduled job
-- never reminds the same person twice for the same activity.
ALTER TABLE activity_participants
    ADD COLUMN reminder_sent_at TIMESTAMPTZ;
