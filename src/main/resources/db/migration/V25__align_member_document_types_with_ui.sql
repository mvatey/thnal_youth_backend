-- Rename this file so V_NEXT is the next Flyway version in your project.
-- Example: V25__align_member_document_types_with_ui.sql

-- 1) Add the exact member-document types used by the UI.
INSERT INTO document_types (
    code,
    label_km,
    label_en,
    description,
    is_active,
    sort_order,
    created_at,
    updated_at
)
VALUES
    (
        'MEMBER_CARD',
        'ប័ណ្ណសមាជិក',
        'Member Card',
        'Member identity card document',
        TRUE,
        10,
        NOW(),
        NOW()
    ),
    (
        'MEMBER_LETTER',
        'លិខិតសមាជិក',
        'Member Letter',
        'Letter belonging to a member',
        TRUE,
        20,
        NOW(),
        NOW()
    ),
    (
        'MEMBER_CERTIFICATE',
        'វិញ្ញាបនបត្រសមាជិក',
        'Member Certificate',
        'Certificate belonging to a member',
        TRUE,
        30,
        NOW(),
        NOW()
    )
ON CONFLICT (code) DO UPDATE
    SET
        label_km = EXCLUDED.label_km,
        label_en = EXCLUDED.label_en,
        description = EXCLUDED.description,
        is_active = TRUE,
        sort_order = EXCLUDED.sort_order,
        updated_at = NOW();

-- 2) Keep the old generic type for backward compatibility, but hide it from
--    new UI selections. Existing rows are not changed automatically.
UPDATE document_types
SET
    is_active = FALSE,
    updated_at = NOW()
WHERE code = 'MEMBER_DOCUMENT';

-- 3) After reviewing existing generic records, migrate each one to the right
--    exact type. Example for the certificate shown during Swagger testing:
--
-- UPDATE documents
-- SET document_type_id = (
--     SELECT id FROM document_types WHERE code = 'MEMBER_CERTIFICATE'
-- )
-- WHERE id = 1
--   AND document_type_id = (
--       SELECT id FROM document_types WHERE code = 'MEMBER_DOCUMENT'
--   );
