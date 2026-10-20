-- Report data (see the member register and the province summary):
--   * a member's national ID number and, when they are on the voter list,
--     their voter card number (both optional);
--   * whether a branch has a location of its own and whether it has been
--     registered according to the law. Every existing branch counts as
--     having both, hence the TRUE defaults.

ALTER TABLE members
    ADD COLUMN IF NOT EXISTS national_id_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS voter_card_number VARCHAR(50);

ALTER TABLE branches
    ADD COLUMN IF NOT EXISTS has_location BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS legally_registered BOOLEAN NOT NULL DEFAULT TRUE;
