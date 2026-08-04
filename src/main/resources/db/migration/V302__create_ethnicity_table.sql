CREATE TABLE ethnicities (
                             id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             code VARCHAR(50) NOT NULL UNIQUE,
                             label_km VARCHAR(100) NOT NULL,
                             label_en VARCHAR(100),
                             is_active BOOLEAN NOT NULL DEFAULT TRUE,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                             CONSTRAINT chk_ethnicity_code_not_blank
                                 CHECK (BTRIM(code) <> ''),

                             CONSTRAINT chk_ethnicity_label_km_not_blank
                                 CHECK (BTRIM(label_km) <> '')
);

INSERT INTO ethnicities (code, label_km, label_en)
VALUES
    ('KHMER', 'ខ្មែរ', 'Khmer'),
    ('CHAM', 'ចាម', 'Cham'),
    ('CHINESE', 'ចិន', 'Chinese'),
    ('VIETNAMESE', 'វៀតណាម', 'Vietnamese'),
    ('OTHER', 'ផ្សេងៗ', 'Other');

ALTER TABLE members
    ADD COLUMN ethnicity_id SMALLINT;

ALTER TABLE members
    ADD CONSTRAINT fk_members_ethnicity
        FOREIGN KEY (ethnicity_id)
            REFERENCES ethnicities(id)
            ON DELETE RESTRICT;

CREATE INDEX idx_members_ethnicity_id
    ON members(ethnicity_id);