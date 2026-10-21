-- A member who came from another political party records which one. A member
-- who did not is, by default, of the Cambodian party this association is
-- part of, so there is nothing to store for them.

ALTER TABLE members
    ADD COLUMN IF NOT EXISTS from_other_party BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS former_party VARCHAR(255);
