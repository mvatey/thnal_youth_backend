INSERT INTO payment_methods (
    code,
    label_km,
    label_en
)
VALUES (
           'MATERIAL',
           'សម្ភារៈ',
           'Material'
       )
ON CONFLICT (code)
    DO UPDATE SET
                  label_km = EXCLUDED.label_km,
                  label_en = EXCLUDED.label_en;


SELECT
    installed_rank,
    version,
    description,
    type,
    script,
    checksum,
    success
FROM flyway_schema_history
WHERE version = '35';


SELECT
    id,
    code,
    label_km,
    label_en
FROM payment_methods
WHERE code = 'MATERIAL';