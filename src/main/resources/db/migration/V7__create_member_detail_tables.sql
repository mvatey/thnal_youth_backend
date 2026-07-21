--35. member_family
CREATE TABLE member_family (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               member_id BIGINT NOT NULL,

                               relationship VARCHAR(20) NOT NULL,

                               full_name_km VARCHAR(255) NOT NULL,
                               full_name_en VARCHAR(255),

                               date_of_birth DATE,
                               occupation VARCHAR(255),

                               life_status VARCHAR(20),

                               address VARCHAR(255),

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_member_family_member
                                   FOREIGN KEY (member_id)
                                       REFERENCES members(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT chk_member_family_name
                                   CHECK (BTRIM(full_name_km) <> ''),

                               CONSTRAINT chk_member_family_relationship
                                   CHECK (
                                       relationship IN (
                                                        'FATHER',
                                                        'MOTHER',
                                                        'SPOUSE'
                                           )
                                       ),

                               CONSTRAINT chk_member_family_life_status
                                   CHECK (
                                       life_status IS NULL
                                           OR life_status IN (
                                                              'ALIVE',
                                                              'DECEASED'
                                           )
                                       )
);

CREATE INDEX idx_member_family_member_id
    ON member_family(member_id);


--36. member_work_history
CREATE TABLE member_work_history (
                                     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                     member_id BIGINT NOT NULL,

                                     organization_name VARCHAR(255) NOT NULL,
                                     position_title VARCHAR(255) NOT NULL,

                                     address VARCHAR(255),

                                     employment_sector_id SMALLINT,

                                     start_date DATE,
                                     end_date DATE,

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                     CONSTRAINT fk_member_work_history_member
                                         FOREIGN KEY (member_id)
                                             REFERENCES members(id)
                                             ON DELETE CASCADE,

                                     CONSTRAINT fk_member_work_history_sector
                                         FOREIGN KEY (employment_sector_id)
                                             REFERENCES employment_sectors(id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT chk_member_work_organization
                                         CHECK (BTRIM(organization_name) <> ''),

                                     CONSTRAINT chk_member_work_position
                                         CHECK (BTRIM(position_title) <> ''),

                                     CONSTRAINT chk_member_work_history_dates
                                         CHECK (
                                             end_date IS NULL
                                                 OR start_date IS NULL
                                                 OR end_date >= start_date
                                             )
);

CREATE INDEX idx_member_work_history_member_id
    ON member_work_history(member_id);

CREATE INDEX idx_member_work_history_sector_id
    ON member_work_history(employment_sector_id);


--37. member_education
CREATE TABLE member_education (
                                  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                  member_id BIGINT NOT NULL,

                                  school_name VARCHAR(255) NOT NULL,
                                  education_level_id SMALLINT NOT NULL,
                                  field_of_study VARCHAR(255),

                                  country_code VARCHAR(2) NOT NULL,
                                  country_name VARCHAR(100) NOT NULL,

                                  province_id SMALLINT,
                                  province_name VARCHAR(150),

                                  certificate_file_id BIGINT,

                                  start_date DATE,
                                  end_date DATE,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                  CONSTRAINT fk_member_education_member
                                      FOREIGN KEY (member_id)
                                          REFERENCES members(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_member_education_level
                                      FOREIGN KEY (education_level_id)
                                          REFERENCES education_levels(id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT fk_member_education_province
                                      FOREIGN KEY (province_id)
                                          REFERENCES provinces(id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT fk_member_education_certificate
                                      FOREIGN KEY (certificate_file_id)
                                          REFERENCES files(id)
                                          ON DELETE SET NULL,

                                  CONSTRAINT chk_member_education_school
                                      CHECK (BTRIM(school_name) <> ''),

                                  CONSTRAINT chk_member_education_country_code
                                      CHECK (
                                          country_code ~ '^[A-Z]{2}$'
),

    CONSTRAINT chk_member_education_country_name
        CHECK (BTRIM(country_name) <> ''),

    CONSTRAINT chk_member_education_dates
        CHECK (
            end_date IS NULL
            OR start_date IS NULL
            OR end_date >= start_date
        ),

    CONSTRAINT chk_member_education_location
        CHECK (
            (
                country_code = 'KH'
                AND province_name IS NULL
            )
            OR
            (
                country_code <> 'KH'
                AND province_id IS NULL
            )
        )
);

CREATE INDEX idx_member_education_member_id
    ON member_education(member_id);

CREATE INDEX idx_member_education_level_id
    ON member_education(education_level_id);


--38. member_skills
CREATE TABLE member_skills (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               member_id BIGINT NOT NULL,

                               skill_name VARCHAR(150) NOT NULL,

                               proficiency_level_id SMALLINT NOT NULL,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_member_skill_member
                                   FOREIGN KEY (member_id)
                                       REFERENCES members(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_member_skill_proficiency
                                   FOREIGN KEY (proficiency_level_id)
                                       REFERENCES proficiency_levels(id)
                                       ON DELETE RESTRICT,

                               CONSTRAINT chk_member_skill_name
                                   CHECK (BTRIM(skill_name) <> '')
);

CREATE UNIQUE INDEX uq_member_skill_case_insensitive
    ON member_skills(member_id, LOWER(BTRIM(skill_name)));


--39. member_languages
CREATE TABLE member_languages (
                                  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                  member_id BIGINT NOT NULL,

                                  language_name VARCHAR(100) NOT NULL,

                                  listening_level_id SMALLINT,
                                  speaking_level_id SMALLINT,
                                  reading_level_id SMALLINT,
                                  writing_level_id SMALLINT,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                  CONSTRAINT fk_member_language_member
                                      FOREIGN KEY (member_id)
                                          REFERENCES members(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_member_language_listening_level
                                      FOREIGN KEY (listening_level_id)
                                          REFERENCES proficiency_levels(id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT fk_member_language_speaking_level
                                      FOREIGN KEY (speaking_level_id)
                                          REFERENCES proficiency_levels(id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT fk_member_language_reading_level
                                      FOREIGN KEY (reading_level_id)
                                          REFERENCES proficiency_levels(id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT fk_member_language_writing_level
                                      FOREIGN KEY (writing_level_id)
                                          REFERENCES proficiency_levels(id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT chk_member_language_name
                                      CHECK (BTRIM(language_name) <> ''),

                                  CONSTRAINT chk_member_language_has_proficiency
                                      CHECK (
                                          listening_level_id IS NOT NULL
                                              OR speaking_level_id IS NOT NULL
                                              OR reading_level_id IS NOT NULL
                                              OR writing_level_id IS NOT NULL
                                          )
);

CREATE UNIQUE INDEX uq_member_language_case_insensitive
    ON member_languages(member_id, LOWER(BTRIM(language_name)));


--40. member_political_affiliations
CREATE TABLE member_political_affiliations (
                                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                               member_id BIGINT NOT NULL,

                                               affiliation_name VARCHAR(255) NOT NULL,
                                               position_title VARCHAR(255),

                                               location VARCHAR(255),

                                               start_date DATE,
                                               end_date DATE,

                                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                               CONSTRAINT fk_member_political_affiliation_member
                                                   FOREIGN KEY (member_id)
                                                       REFERENCES members(id)
                                                       ON DELETE CASCADE,

                                               CONSTRAINT chk_member_political_affiliation_name
                                                   CHECK (BTRIM(affiliation_name) <> ''),

                                               CONSTRAINT chk_member_political_affiliation_dates
                                                   CHECK (
                                                       end_date IS NULL
                                                           OR start_date IS NULL
                                                           OR end_date >= start_date
                                                       )
);

CREATE INDEX idx_member_political_affiliation_member_id
    ON member_political_affiliations(member_id);


--41. member_credentials
CREATE TABLE member_credentials (
                                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                    member_id BIGINT NOT NULL,
                                    activity_id BIGINT,

                                    credential_kind VARCHAR(30) NOT NULL,
                                    credential_no VARCHAR(100) NOT NULL UNIQUE,

                                    title VARCHAR(255) NOT NULL,
                                    issued_on DATE NOT NULL,

                                    issued_by BIGINT NOT NULL,
                                    file_id BIGINT,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                    CONSTRAINT fk_member_credential_member
                                        FOREIGN KEY (member_id)
                                            REFERENCES members(id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_member_credential_activity
                                        FOREIGN KEY (activity_id)
                                            REFERENCES activities(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT fk_member_credential_issuer
                                        FOREIGN KEY (issued_by)
                                            REFERENCES users(id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT fk_member_credential_file
                                        FOREIGN KEY (file_id)
                                            REFERENCES files(id)
                                            ON DELETE SET NULL,

                                    CONSTRAINT chk_member_credential_number
                                        CHECK (BTRIM(credential_no) <> ''),

                                    CONSTRAINT chk_member_credential_title
                                        CHECK (BTRIM(title) <> ''),

                                    CONSTRAINT chk_member_credential_kind
                                        CHECK (
                                            credential_kind IN (
                                                                'MEMBERSHIP_CARD',
                                                                'ACTIVITY_CERTIFICATE'
                                                )
                                            ),

                                    CONSTRAINT chk_member_credential_activity
                                        CHECK (
                                            (
                                                credential_kind = 'ACTIVITY_CERTIFICATE'
                                                    AND activity_id IS NOT NULL
                                                )
                                                OR
                                            (
                                                credential_kind = 'MEMBERSHIP_CARD'
                                                    AND activity_id IS NULL
                                                )
                                            )
);

CREATE INDEX idx_member_credentials_member_id
    ON member_credentials(member_id);

CREATE INDEX idx_member_credentials_activity_id
    ON member_credentials(activity_id);

CREATE INDEX idx_member_credentials_issued_by
    ON member_credentials(issued_by);
