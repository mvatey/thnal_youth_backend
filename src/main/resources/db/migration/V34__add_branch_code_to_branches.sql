ALTER TABLE branches
    ADD COLUMN IF NOT EXISTS branch_code VARCHAR(100);

UPDATE branches
SET branch_code = 'BR-' || LPAD(id::TEXT, 4, '0')
WHERE branch_code IS NULL;

ALTER TABLE branches
    ALTER COLUMN branch_code SET NOT NULL;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'uk_branches_branch_code'
        ) THEN
            ALTER TABLE branches
                ADD CONSTRAINT uk_branches_branch_code
                    UNIQUE (branch_code);
        END IF;
    END
$$;