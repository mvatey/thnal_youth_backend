-- Keep the database aligned with the current Java/API dropdown values.
-- Gender.java uses MALE, FEMALE, MONK.
-- TshirtSize uses display/database values XS, S, M, L, XL, 2XL, 3XL.

ALTER TABLE members
    DROP CONSTRAINT IF EXISTS chk_member_gender;

-- Older schemas used OTHER while the current application uses MONK.
-- Normalize any legacy rows before enforcing the current allowed values.
UPDATE members
SET gender = 'MONK'
WHERE gender = 'OTHER';

ALTER TABLE members
    ADD CONSTRAINT chk_member_gender
        CHECK (gender IN ('MALE', 'FEMALE', 'MONK'));

ALTER TABLE members
    DROP CONSTRAINT IF EXISTS chk_member_tshirt_size;

-- Defensive normalization for databases that may have received Java enum names
-- before the converter was added.
UPDATE members
SET tshirt_size = '2XL'
WHERE tshirt_size = 'TWO_XL';

UPDATE members
SET tshirt_size = '3XL'
WHERE tshirt_size = 'THREE_XL';

ALTER TABLE members
    ADD CONSTRAINT chk_member_tshirt_size
        CHECK (
            tshirt_size IS NULL
            OR tshirt_size IN ('XS', 'S', 'M', 'L', 'XL', '2XL', '3XL')
        );
