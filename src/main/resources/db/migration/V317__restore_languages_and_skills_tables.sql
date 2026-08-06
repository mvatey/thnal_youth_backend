/*
 * ============================================================
 * Language dropdown lookup
 * ============================================================
 */

CREATE TABLE IF NOT EXISTS languages (
                                         id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                         code VARCHAR(50) NOT NULL UNIQUE,

    label_km VARCHAR(100) NOT NULL,

    label_en VARCHAR(100) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    sort_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_language_code
    CHECK (btrim(code) <> ''),

    CONSTRAINT chk_language_label_km
    CHECK (btrim(label_km) <> ''),

    CONSTRAINT chk_language_label_en
    CHECK (btrim(label_en) <> '')
    );

INSERT INTO languages (
    code,
    label_km,
    label_en,
    is_active,
    sort_order
)
VALUES
    (
        'ENGLISH',
        'ភាសាអង់គ្លេស',
        'English',
        TRUE,
        1
    ),
    (
        'CHINESE',
        'ភាសាចិន',
        'Chinese',
        TRUE,
        2
    ),
    (
        'FRENCH',
        'ភាសាបារាំង',
        'French',
        TRUE,
        3
    ),
    (
        'FILIPINO',
        'ភាសាហ្វីលីពីន',
        'Filipino',
        TRUE,
        4
    ),
    (
        'JAPANESE',
        'ភាសាជប៉ុន',
        'Japanese',
        TRUE,
        5
    ),
    (
        'KOREAN',
        'ភាសាកូរ៉េ',
        'Korean',
        TRUE,
        6
    ),
    (
        'OTHER',
        'ផ្សេងៗ',
        'Other',
        TRUE,
        99
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
 * Skill dropdown lookup
 * ============================================================
 */

CREATE TABLE IF NOT EXISTS skills (
                                      id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                      code VARCHAR(100) NOT NULL UNIQUE,

    label_km VARCHAR(150) NOT NULL,

    label_en VARCHAR(150) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    sort_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_skill_code
    CHECK (btrim(code) <> ''),

    CONSTRAINT chk_skill_label_km
    CHECK (btrim(label_km) <> ''),

    CONSTRAINT chk_skill_label_en
    CHECK (btrim(label_en) <> '')
    );

INSERT INTO skills (
    code,
    label_km,
    label_en,
    is_active,
    sort_order
)
VALUES
    (
        'MICROSOFT_WORD',
        'Microsoft Word',
        'Microsoft Word',
        TRUE,
        1
    ),
    (
        'MICROSOFT_EXCEL',
        'Microsoft Excel',
        'Microsoft Excel',
        TRUE,
        2
    ),
    (
        'MICROSOFT_POWERPOINT',
        'Microsoft PowerPoint',
        'Microsoft PowerPoint',
        TRUE,
        3
    ),
    (
        'GRAPHIC_DESIGN',
        'រចនាក្រាហ្វិក',
        'Graphic Design',
        TRUE,
        4
    ),
    (
        'VIDEO_EDITING',
        'កាត់តវីដេអូ',
        'Video Editing',
        TRUE,
        5
    ),
    (
        'PROGRAMMING',
        'ការសរសេរកម្មវិធី',
        'Programming',
        TRUE,
        6
    ),
    (
        'OTHER',
        'ផ្សេងៗ',
        'Other',
        TRUE,
        99
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              is_active = EXCLUDED.is_active,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();