/*
 * ============================================================
 * Restore member_languages.language_name
 * ============================================================
 */

ALTER TABLE member_languages
    ADD COLUMN IF NOT EXISTS language_name VARCHAR(100);

/*
 * Restore names from the languages lookup table before
 * removing language_id.
 */
UPDATE member_languages ml
SET language_name = l.label_en
    FROM languages l
WHERE ml.language_id = l.id
  AND (
    ml.language_name IS NULL
   OR btrim(ml.language_name) = ''
    );

/*
 * Stop migration if an existing record could not be restored.
 */
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM member_languages
        WHERE language_name IS NULL
           OR btrim(language_name) = ''
    ) THEN
        RAISE EXCEPTION
            'Some member language records could not be restored';
END IF;
END
$$;

ALTER TABLE member_languages
    ALTER COLUMN language_name SET NOT NULL;

ALTER TABLE member_languages
DROP CONSTRAINT IF EXISTS uq_member_language;

ALTER TABLE member_languages
DROP CONSTRAINT IF EXISTS fk_member_language_language;

ALTER TABLE member_languages
DROP COLUMN IF EXISTS language_id;

ALTER TABLE member_languages
DROP CONSTRAINT IF EXISTS chk_member_language_name;

ALTER TABLE member_languages
    ADD CONSTRAINT chk_member_language_name
        CHECK (btrim(language_name) <> '');

DROP INDEX IF EXISTS uq_member_language_case_insensitive;

CREATE UNIQUE INDEX uq_member_language_case_insensitive
    ON member_languages (
                         member_id,
                         lower(btrim(language_name))
        );


/*
 * ============================================================
 * Restore member_skills.skill_name
 * ============================================================
 */

ALTER TABLE member_skills
    ADD COLUMN IF NOT EXISTS skill_name VARCHAR(150);

/*
 * Restore names from the skills lookup table before removing
 * skill_id.
 */
UPDATE member_skills ms
SET skill_name = s.label_en
    FROM skills s
WHERE ms.skill_id = s.id
  AND (
    ms.skill_name IS NULL
   OR btrim(ms.skill_name) = ''
    );

/*
 * Stop migration if an existing record could not be restored.
 */
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM member_skills
        WHERE skill_name IS NULL
           OR btrim(skill_name) = ''
    ) THEN
        RAISE EXCEPTION
            'Some member skill records could not be restored';
END IF;
END
$$;

ALTER TABLE member_skills
    ALTER COLUMN skill_name SET NOT NULL;

ALTER TABLE member_skills
DROP CONSTRAINT IF EXISTS uq_member_skill;

ALTER TABLE member_skills
DROP CONSTRAINT IF EXISTS fk_member_skill_skill;

ALTER TABLE member_skills
DROP COLUMN IF EXISTS skill_id;

ALTER TABLE member_skills
DROP CONSTRAINT IF EXISTS chk_member_skill_name;

ALTER TABLE member_skills
    ADD CONSTRAINT chk_member_skill_name
        CHECK (btrim(skill_name) <> '');

DROP INDEX IF EXISTS uq_member_skill_case_insensitive;

CREATE UNIQUE INDEX uq_member_skill_case_insensitive
    ON member_skills (
                      member_id,
                      lower(btrim(skill_name))
        );


/*
 * ============================================================
 * Keep certificate columns
 * ============================================================
 *
 * The certificate_file_id columns and foreign keys remain.
 * They are required by the language and skill attachment APIs.
 */


/*
 * ============================================================
 * Remove unused lookup tables
 * ============================================================
 */

DROP TABLE IF EXISTS languages;
DROP TABLE IF EXISTS skills;