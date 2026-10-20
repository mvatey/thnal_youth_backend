-- A single COUNTRY-level branch that sits above every province branch.
--
-- It covers the whole country, so it has no province/district/commune of
-- its own: branches.province_id becomes nullable, but ONLY for that level
-- (enforced below). Which branches it covers is never stored -- the
-- backend computes it live (see StaffBranchScopeService), so new areas are
-- included automatically.

-- The id is fixed at 4 (after PROVINCE/DISTRICT/COMMUNE = 1/2/3) because the
-- branch forms address the levels by those ids; the sequence is moved past
-- it so a later level can't collide.
INSERT INTO branch_levels (
    id,
    code,
    label_km,
    label_en,
    hierarchy_order,
    description,
    sort_order
)
OVERRIDING SYSTEM VALUE
VALUES (
    4,
    'COUNTRY',
    'សាខាថ្នាក់ជាតិ',
    'Country Branch',
    4,
    'សាខាថ្នាក់ជាតិដែលគ្របដណ្ដប់លើគ្រប់សាខាទាំងអស់',
    0
)
ON CONFLICT (code) DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('branch_levels', 'id'),
    GREATEST(4, (SELECT MAX(id) FROM branch_levels))
);

ALTER TABLE branches
    ALTER COLUMN province_id DROP NOT NULL;

DO $$
    DECLARE
        country_level_id SMALLINT;
    BEGIN
        SELECT id
        INTO country_level_id
        FROM branch_levels
        WHERE code = 'COUNTRY';

        -- Every branch except the country one still needs a province.
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'chk_branch_province_required'
        ) THEN
            EXECUTE format(
                'ALTER TABLE branches
                     ADD CONSTRAINT chk_branch_province_required
                         CHECK (province_id IS NOT NULL OR branch_level_id = %s)',
                country_level_id
            );
        END IF;

        -- And there is exactly one country branch.
        EXECUTE format(
            'CREATE UNIQUE INDEX IF NOT EXISTS uq_branch_single_country
                 ON branches ((1))
                 WHERE branch_level_id = %s',
            country_level_id
        );
    END
$$;
