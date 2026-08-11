CREATE TABLE document_options (
    id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category VARCHAR(30) NOT NULL,
    code VARCHAR(50) NOT NULL,
    value VARCHAR(100) NOT NULL,
    label_km VARCHAR(100) NOT NULL,
    label_en VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_document_options_category_code UNIQUE (category, code),
    CONSTRAINT chk_document_options_category
        CHECK (category IN ('FONT', 'LANGUAGE', 'CARD_SIZE')),
    CONSTRAINT chk_document_options_code CHECK (BTRIM(code) <> ''),
    CONSTRAINT chk_document_options_value CHECK (BTRIM(value) <> ''),
    CONSTRAINT chk_document_options_label_km CHECK (BTRIM(label_km) <> ''),
    CONSTRAINT chk_document_options_label_en CHECK (BTRIM(label_en) <> '')
);

CREATE INDEX idx_document_options_active_sort
    ON document_options(category, is_active, sort_order, id);

INSERT INTO document_options
    (category, code, value, label_km, label_en, sort_order)
VALUES
    ('FONT', 'NOTO_SANS', 'Noto Sans', 'Noto Sans Khmer', 'Noto Sans Khmer', 1),
    ('FONT', 'KANTUMRUY_PRO', 'Kantumruy Pro', 'Kantumruy Pro', 'Kantumruy Pro', 2),
    ('LANGUAGE', 'KHMER', 'km', 'ភាសាខ្មែរ', 'Khmer', 1),
    ('LANGUAGE', 'ENGLISH', 'en', 'ភាសាអង់គ្លេស', 'English', 2),
    ('CARD_SIZE', 'SMALL', '650', '650 px', '650 px', 1),
    ('CARD_SIZE', 'LARGE', '780', '780 px', '780 px', 2)
ON CONFLICT (category, code) DO NOTHING;

INSERT INTO document_types
    (code, label_km, label_en, description, sort_order)
VALUES
    ('MEMBER_CERTIFICATE', 'វិញ្ញាបនបត្រសមាជិក', 'Member Certificate',
     'A certificate issued to a member.', 6),
    ('APPOINTMENT_LETTER', 'លិខិតតែងតាំង', 'Letter of Appointment',
     'An appointment letter issued to a member.', 7),
    ('MEMBER_ID_CARD', 'ប័ណ្ណសម្គាល់សមាជិក', 'Member ID Card',
     'A membership identification card.', 8)
ON CONFLICT (code) DO UPDATE SET
    label_km = EXCLUDED.label_km,
    label_en = EXCLUDED.label_en,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order,
    updated_at = NOW();
