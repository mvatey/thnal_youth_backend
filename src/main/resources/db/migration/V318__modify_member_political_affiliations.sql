/*
 * ============================================================
 * 1. Create political-party lookup table first
 * ============================================================
 */

CREATE TABLE IF NOT EXISTS political_parties (
                                                 id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                                 code VARCHAR(100) NOT NULL UNIQUE,

    label_km VARCHAR(255) NOT NULL,

    label_en VARCHAR(255) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    sort_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_political_party_code
    CHECK (btrim(code) <> ''),

    CONSTRAINT chk_political_party_label_km
    CHECK (btrim(label_km) <> ''),

    CONSTRAINT chk_political_party_label_en
    CHECK (btrim(label_en) <> '')
    );


/*
 * Seed at least OTHER so existing affiliation rows can be migrated.
 * Add the actual political parties later.
 */

INSERT INTO political_parties (
    code,
    label_km,
    label_en,
    is_active,
    sort_order
)
VALUES (
           'OTHER',
           'ផ្សេងៗ',
           'Other',
           TRUE,
           999
       )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              is_active = EXCLUDED.is_active,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();


/*
 * ============================================================
 * 2. Add the new fields
 * ============================================================
 */

ALTER TABLE member_political_affiliations
    ADD COLUMN IF NOT EXISTS party_id SMALLINT;

ALTER TABLE member_political_affiliations
    ADD COLUMN IF NOT EXISTS country VARCHAR(100);

ALTER TABLE member_political_affiliations
    ADD COLUMN IF NOT EXISTS card_no VARCHAR(100);

ALTER TABLE member_political_affiliations
    ADD COLUMN IF NOT EXISTS is_current BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE member_political_affiliations
    ADD COLUMN IF NOT EXISTS note TEXT;


/*
 * ============================================================
 * 3. Assign existing records to OTHER
 * ============================================================
 */

UPDATE member_political_affiliations
SET party_id = (
    SELECT id
    FROM political_parties
    WHERE code = 'OTHER'
)
WHERE party_id IS NULL;


/*
 * ============================================================
 * 4. Make party required and add foreign key
 * ============================================================
 */

ALTER TABLE member_political_affiliations
    ALTER COLUMN party_id SET NOT NULL;

ALTER TABLE member_political_affiliations
DROP CONSTRAINT IF EXISTS
        fk_member_political_affiliation_party;

ALTER TABLE member_political_affiliations
    ADD CONSTRAINT fk_member_political_affiliation_party
        FOREIGN KEY (party_id)
            REFERENCES political_parties(id)
            ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS
    idx_member_political_affiliation_party_id
    ON member_political_affiliations(party_id);


/*
 * ============================================================
 * 5. Remove old affiliation-name field
 * ============================================================
 */

ALTER TABLE member_political_affiliations
DROP CONSTRAINT IF EXISTS
        chk_member_political_affiliation_name;

ALTER TABLE member_political_affiliations
DROP COLUMN IF EXISTS affiliation_name;


/*
 * ============================================================
 * 6. Recreate date/current validation
 * ============================================================
 */

ALTER TABLE member_political_affiliations
DROP CONSTRAINT IF EXISTS
        chk_member_political_affiliation_dates;

ALTER TABLE member_political_affiliations
    ADD CONSTRAINT chk_member_political_affiliation_dates
        CHECK (
            end_date IS NULL
                OR start_date IS NULL
                OR end_date >= start_date
            );

ALTER TABLE member_political_affiliations
DROP CONSTRAINT IF EXISTS
        chk_member_political_affiliation_current;

ALTER TABLE member_political_affiliations
    ADD CONSTRAINT chk_member_political_affiliation_current
        CHECK (
            is_current = FALSE
                OR end_date IS NULL
            );