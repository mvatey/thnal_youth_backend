INSERT INTO payment_methods (
    code,
    label_km,
    label_en,
    category
)
VALUES (
           'MATERIAL',
           'សម្ភារៈ',
           'Material',
           'OTHER'
       )
ON CONFLICT (code)
    DO UPDATE SET
                  label_km = EXCLUDED.label_km,
                  label_en = EXCLUDED.label_en,
                  category = EXCLUDED.category;
