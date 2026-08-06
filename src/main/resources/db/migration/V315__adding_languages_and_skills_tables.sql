CREATE TABLE languages (
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
    sort_order
)
VALUES
    ('ENGLISH', 'ភាសាអង់គ្លេស', 'English', 1),
    ('CHINESE', 'ភាសាចិន', 'Chinese', 2),
    ('FRENCH', 'ភាសាបារាំង', 'French', 3),
    ('Filipino', 'ភាសាហ្វីលីពីន', 'Filipino', 4),
    ('JAPANESE', 'ភាសាជប៉ុន', 'Japanese', 5),
    ('KOREAN', 'ភាសាកូរ៉េ', 'Korean', 6),
    ('OTHER', 'ផ្សេងៗ', 'Other', 99);

CREATE TABLE skills (
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
    sort_order
)
VALUES
    ('MICROSOFT_WORD', 'Microsoft Word', 'Microsoft Word', 1),
    ('MICROSOFT_EXCEL', 'Microsoft Excel', 'Microsoft Excel', 2),
    ('MICROSOFT_POWERPOINT', 'Microsoft PowerPoint', 'Microsoft PowerPoint', 3),
    ('GRAPHIC_DESIGN', 'រចនាក្រាហ្វិក', 'Graphic Design', 4),
    ('VIDEO_EDITING', 'កាត់តវីដេអូ', 'Video Editing', 5),
    ('PROGRAMMING', 'ការសរសេរកម្មវិធី', 'Programming', 6),
    ('OTHER', 'ផ្សេងៗ', 'Other', 99);

ALTER TABLE member_languages
    ADD COLUMN language_id SMALLINT;

ALTER TABLE member_languages
    ADD CONSTRAINT fk_member_language_language
        FOREIGN KEY (language_id)
            REFERENCES languages(id)
            ON DELETE RESTRICT;

UPDATE member_languages ml
SET language_id = l.id
    FROM languages l
WHERE lower(btrim(ml.language_name))
    = lower(btrim(l.label_en));

ALTER TABLE member_languages
    ALTER COLUMN language_id SET NOT NULL;

DROP INDEX IF EXISTS
uq_member_language_case_insensitive;

ALTER TABLE member_languages
    ADD CONSTRAINT uq_member_language
        UNIQUE (member_id, language_id);

ALTER TABLE member_languages
DROP COLUMN language_name;

ALTER TABLE member_skills
    ADD COLUMN skill_id SMALLINT;

ALTER TABLE member_skills
    ADD CONSTRAINT fk_member_skill_skill
        FOREIGN KEY (skill_id)
            REFERENCES skills(id)
            ON DELETE RESTRICT;

UPDATE member_skills ms
SET skill_id = s.id
    FROM skills s
WHERE lower(btrim(ms.skill_name))
    = lower(btrim(s.label_en));

ALTER TABLE member_skills
    ALTER COLUMN skill_id SET NOT NULL;

DROP INDEX IF EXISTS
uq_member_skill_case_insensitive;

ALTER TABLE member_skills
    ADD CONSTRAINT uq_member_skill
        UNIQUE (member_id, skill_id);

ALTER TABLE member_skills
DROP COLUMN skill_name;

----------------

ALTER TABLE member_languages
    ADD COLUMN certificate_file_id BIGINT;

ALTER TABLE member_languages
    ADD CONSTRAINT fk_member_language_certificate
        FOREIGN KEY (certificate_file_id)
            REFERENCES files(id)
            ON DELETE SET NULL;

ALTER TABLE member_skills
    ADD COLUMN certificate_file_id BIGINT;

ALTER TABLE member_skills
    ADD CONSTRAINT fk_member_skill_certificate
        FOREIGN KEY (certificate_file_id)
            REFERENCES files(id)
            ON DELETE SET NULL;
