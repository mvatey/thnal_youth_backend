INSERT INTO political_parties (
    code,
    label_km,
    label_en,
    is_active,
    sort_order
)
VALUES
    (
        'CPP',
        'គណបក្សប្រជាជនកម្ពុជា',
        'Cambodian People''s Party',
        TRUE,
        1
    )ON CONFLICT (code) DO UPDATE
SET
    label_km = EXCLUDED.label_km,
    label_en = EXCLUDED.label_en,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order,
    updated_at = NOW();