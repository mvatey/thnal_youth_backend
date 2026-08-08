UPDATE branches b
SET
    branch_code =
        'BR-P-' || LPAD(b.province_id::text, 3, '0'),

    name_km =
        p.name_km,

    name_en =
        p.name_en,

    updated_at = CURRENT_TIMESTAMP
    FROM provinces p
WHERE b.province_id = p.id
  AND b.branch_code IN (
    'DASH-DEMO-BRANCH-A',
    'DASH-DEMO-BRANCH-B',
    'DASH-DEMO-BRANCH-C'
    );