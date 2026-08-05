-- ============================================================
-- Seed Phnom Penh province safely
-- ============================================================

UPDATE provinces
SET
    code = 'PP',
    name_en = 'Phnom Penh'
WHERE LOWER(BTRIM(name_km))
          = LOWER(BTRIM('រាជធានីភ្នំពេញ'));

INSERT INTO provinces (
    code,
    name_km,
    name_en
)
SELECT
    'PP',
    'រាជធានីភ្នំពេញ',
    'Phnom Penh'
    WHERE NOT EXISTS (
    SELECT 1
    FROM provinces
    WHERE code = 'PP'
       OR LOWER(BTRIM(name_km))
          = LOWER(BTRIM('រាជធានីភ្នំពេញ'))
);

-- ------------------------------------------------------------
-- 2. Seed one root/province-level branch
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
      AND b.parent_branch_id IS NULL
      AND b.district_id IS NULL
      AND b.commune_id IS NULL
);