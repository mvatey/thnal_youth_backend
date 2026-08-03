-- ============================================================
-- V19: Seed minimum data required to test Activity creation
-- ============================================================

-- ------------------------------------------------------------
-- 1. Seed Phnom Penh province
-- ------------------------------------------------------------
-- Required province fields:
-- code, name_km, name_en
--
-- is_active uses its database default TRUE.
-- We intentionally do not use sort_order here.
-- ------------------------------------------------------------

INSERT INTO provinces (
    code,
    name_km,
    name_en
)
VALUES (
           'PP',
           'រាជធានីភ្នំពេញ',
           'Phnom Penh'
       )
ON CONFLICT (code)
    DO UPDATE SET
                  name_km = EXCLUDED.name_km,
                  name_en = EXCLUDED.name_en;


-- ------------------------------------------------------------
-- 2. Seed one root/province-level branch
-- ------------------------------------------------------------
-- branch_level_id comes from branch_levels.code = PROVINCE
-- status_id comes from branch_statuses.code = ACTIVE
-- province_id comes from provinces.code = PP
-- created_by uses the first ADMIN user
-- ------------------------------------------------------------

INSERT INTO branches (
    name_km,
    name_en,
    branch_level_id,
    parent_branch_id,
    province_id,
    district_id,
    commune_id,
    status_id,
    address,
    google_map_url,
    phone,
    email,
    created_by
)
SELECT
    'សាខាកណ្ដាល',
    'Head Office',
    bl.id,
    NULL,
    p.id,
    NULL,
    NULL,
    bs.id,
    'រាជធានីភ្នំពេញ',
    NULL,
    NULL,
    NULL,
    (
        SELECT MIN(u.id)
        FROM users u
        WHERE u.role = 'ADMIN'
    )
FROM branch_levels bl
         JOIN provinces p
              ON p.code = 'PP'
         JOIN branch_statuses bs
              ON bs.code = 'ACTIVE'
WHERE bl.code = 'PROVINCE'
  AND NOT EXISTS (
    SELECT 1
    FROM branches b
    WHERE LOWER(BTRIM(b.name_km))
        = LOWER(BTRIM('សាខាកណ្ដាល'))
      AND b.province_id = p.id
      AND b.district_id IS NULL
      AND b.commune_id IS NULL
);