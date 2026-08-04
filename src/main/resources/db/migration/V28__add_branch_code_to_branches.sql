-- add branch_code

ALTER TABLE branches
    ADD COLUMN IF NOT EXISTS branch_code VARCHAR(50);