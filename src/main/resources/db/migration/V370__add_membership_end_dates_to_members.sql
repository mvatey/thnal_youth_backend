-- The date a member resigned or was fired (one date: the status says which),
-- and the date a member passed away. Asked for when the status or life status
-- is set; the member register prints them.

ALTER TABLE members
    ADD COLUMN IF NOT EXISTS membership_ended_on DATE,
    ADD COLUMN IF NOT EXISTS deceased_on DATE;
