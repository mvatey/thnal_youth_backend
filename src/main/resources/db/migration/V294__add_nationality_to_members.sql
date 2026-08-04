CREATE TABLE nationalities (
                               id SMALLSERIAL PRIMARY KEY,
                               code VARCHAR(50) NOT NULL UNIQUE,
                               label_km VARCHAR(100) NOT NULL,
                               label_en VARCHAR(100) NOT NULL,
                               display_order INTEGER NOT NULL DEFAULT 0,
                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO nationalities (
    code,
    label_km,
    label_en,
    display_order,
    is_active
)
VALUES
    ('KHMER', 'ខ្មែរ', 'Khmer', 1, TRUE),
    ('VIETNAMESE', 'វៀតណាម', 'Vietnamese', 2, TRUE),
    ('CHINESE', 'ចិន', 'Chinese', 4, TRUE),
    ('JAPANESE', 'ជប៉ុន', 'Japanese', 5, TRUE),
    ('OTHER', 'ផ្សេងៗ', 'Other', 99, TRUE);

ALTER TABLE members
    ADD COLUMN nationality_id SMALLINT;

ALTER TABLE members
    ADD CONSTRAINT fk_members_nationality
        FOREIGN KEY (nationality_id)
            REFERENCES nationalities(id);

CREATE INDEX idx_members_nationality_id
    ON members(nationality_id);