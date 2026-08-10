-- V24: audit + edit-conflict support for the donation module.
--
-- V8 tracks WHO recorded a donation (recorded_by) and WHEN it was last touched
-- (updated_at), but not WHO edited it. For a financial record that is a
-- governance gap: a correction to an amount or donor leaves no attributable
-- trail. This migration adds the editor column, backward-compatibly.
--
-- It does NOT change any V8/V23 column or constraint (V1-V23 are immutable once
-- applied). The optimistic-lock guard added in the same change needs NO schema
-- change — it reuses the existing updated_at column as the version token.

-- ------------------------------------------------------------
-- Last-editor attribution
-- ------------------------------------------------------------
-- Nullable: a freshly recorded donation has never been edited, so updated_by is
-- NULL until the first PUT. ON DELETE SET NULL mirrors recorded_by's intent —
-- removing a staff account must not cascade-delete financial history.
ALTER TABLE donations
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

ALTER TABLE donations
    ADD CONSTRAINT fk_donation_updated_by
        FOREIGN KEY (updated_by)
            REFERENCES users(id)
            ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_donations_updated_by
    ON donations(updated_by);
