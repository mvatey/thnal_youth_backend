--24. files
CREATE TABLE files (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                       file_path TEXT NOT NULL UNIQUE,

                       original_name VARCHAR(255) NOT NULL,
                       mime_type VARCHAR(100) NOT NULL,

                       size_bytes BIGINT NOT NULL,

                       uploaded_by BIGINT,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT chk_file_path
                           CHECK (BTRIM(file_path) <> ''),

                       CONSTRAINT chk_file_original_name
                           CHECK (BTRIM(original_name) <> ''),

                       CONSTRAINT chk_file_mime_type
                           CHECK (BTRIM(mime_type) <> ''),

                       CONSTRAINT chk_file_size
                           CHECK (size_bytes > 0)
);

CREATE INDEX idx_files_uploaded_by
    ON files(uploaded_by);

CREATE INDEX idx_files_created_at
    ON files(created_at DESC);

CREATE INDEX idx_files_mime_type
    ON files(mime_type);


--25. branches
CREATE TABLE branches (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                          name_km VARCHAR(255) NOT NULL,
                          name_en VARCHAR(255),

                          branch_level_id SMALLINT NOT NULL,
                          parent_branch_id BIGINT,

                          province_id SMALLINT NOT NULL,
                          district_id INT,
                          commune_id INT,

                          status_id SMALLINT NOT NULL,

                          address TEXT,
                          google_map_url TEXT,

                          phone VARCHAR(30),
                          email CITEXT,

                          created_by BIGINT,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_branch_level
                              FOREIGN KEY (branch_level_id)
                                  REFERENCES branch_levels(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_branch_parent
                              FOREIGN KEY (parent_branch_id)
                                  REFERENCES branches(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_branch_province
                              FOREIGN KEY (province_id)
                                  REFERENCES provinces(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_branch_district
                              FOREIGN KEY (district_id)
                                  REFERENCES districts(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_branch_commune
                              FOREIGN KEY (commune_id)
                                  REFERENCES communes(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_branch_status
                              FOREIGN KEY (status_id)
                                  REFERENCES branch_statuses(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT chk_branch_name_km
                              CHECK (BTRIM(name_km) <> ''),

                          CONSTRAINT chk_branch_not_own_parent
                              CHECK (
                                  parent_branch_id IS NULL
                                      OR parent_branch_id <> id
                                  ),

                          CONSTRAINT chk_branch_location_selection
                              CHECK (
                                  district_id IS NOT NULL
                                      OR commune_id IS NULL
                                  ),

                          CONSTRAINT chk_branch_phone
                              CHECK (
                                  phone IS NULL
                                      OR BTRIM(phone) <> ''
                                  ),

                          CONSTRAINT chk_branch_email
                              CHECK (
                                  email IS NULL
                                      OR BTRIM(email::TEXT) <> ''
                                  )
);

CREATE INDEX idx_branches_level_id
    ON branches(branch_level_id);

CREATE INDEX idx_branches_parent_id
    ON branches(parent_branch_id);

CREATE INDEX idx_branches_province_id
    ON branches(province_id);

CREATE INDEX idx_branches_district_id
    ON branches(district_id);

CREATE INDEX idx_branches_commune_id
    ON branches(commune_id);

CREATE INDEX idx_branches_status_id
    ON branches(status_id);

CREATE UNIQUE INDEX uq_branches_name_location
    ON branches (
                 LOWER(BTRIM(name_km)),
                 province_id,
                 COALESCE(district_id, 0),
                 COALESCE(commune_id, 0)
        );


--26. members
CREATE TABLE members (
                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                         member_no VARCHAR(50) NOT NULL UNIQUE,

                         full_name_km VARCHAR(255) NOT NULL,
                         full_name_en VARCHAR(255),

                         branch_id BIGINT NOT NULL,
                         status_id SMALLINT NOT NULL,
                         level_id SMALLINT,
                         religion_id SMALLINT,

                         gender VARCHAR(20) NOT NULL,

                         date_of_birth DATE,
                         place_of_birth TEXT,

                         phone VARCHAR(30),
                         email CITEXT,

                         current_address TEXT,
                         permanent_address TEXT,

                         profile_photo_id BIGINT,
                         cv_file_id BIGINT,

                         joined_on DATE,

                         bio TEXT,

                         created_by BIGINT,

                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                         CONSTRAINT fk_member_branch
                             FOREIGN KEY (branch_id)
                                 REFERENCES branches(id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT fk_member_status
                             FOREIGN KEY (status_id)
                                 REFERENCES member_statuses(id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT fk_member_level
                             FOREIGN KEY (level_id)
                                 REFERENCES member_levels(id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT fk_member_religion
                             FOREIGN KEY (religion_id)
                                 REFERENCES religions(id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT fk_member_profile_photo
                             FOREIGN KEY (profile_photo_id)
                                 REFERENCES files(id)
                                 ON DELETE SET NULL,

                         CONSTRAINT fk_member_cv_file
                             FOREIGN KEY (cv_file_id)
                                 REFERENCES files(id)
                                 ON DELETE SET NULL,

                         CONSTRAINT chk_member_number_not_blank
                             CHECK (BTRIM(member_no) <> ''),

                         CONSTRAINT chk_member_name_not_blank
                             CHECK (BTRIM(full_name_km) <> ''),

                         CONSTRAINT chk_member_gender
                             CHECK (
                                 gender IN ('MALE', 'FEMALE', 'OTHER')
                                 ),

                         CONSTRAINT chk_member_phone_not_blank
                             CHECK (
                                 phone IS NULL
                                     OR BTRIM(phone) <> ''
                                 ),

                         CONSTRAINT chk_member_email_not_blank
                             CHECK (
                                 email IS NULL
                                     OR BTRIM(email::TEXT) <> ''
                                 )
);

CREATE INDEX idx_members_branch_id
    ON members(branch_id);

CREATE INDEX idx_members_status_id
    ON members(status_id);

CREATE INDEX idx_members_level_id
    ON members(level_id);

CREATE INDEX idx_members_religion_id
    ON members(religion_id);

CREATE INDEX idx_members_created_by
    ON members(created_by);

CREATE INDEX idx_members_full_name_km
    ON members(full_name_km);

CREATE UNIQUE INDEX uq_members_phone
    ON members(phone)
    WHERE phone IS NOT NULL;

CREATE UNIQUE INDEX uq_members_email
    ON members(email)
    WHERE email IS NOT NULL;


--27. users
CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                       member_id BIGINT UNIQUE,

                       phone VARCHAR(30) UNIQUE,
                       email CITEXT UNIQUE,

                       password_hash TEXT NOT NULL,

                       role_id SMALLINT NOT NULL,
                       account_status_id SMALLINT NOT NULL,

                       last_login_at TIMESTAMPTZ,

                       failed_login_count INT NOT NULL DEFAULT 0,
                       locked_until TIMESTAMPTZ,

                       created_by BIGINT,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT fk_user_member
                           FOREIGN KEY (member_id)
                               REFERENCES members(id)
                               ON DELETE SET NULL,

                       CONSTRAINT fk_user_role
                           FOREIGN KEY (role_id)
                               REFERENCES roles(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT fk_user_account_status
                           FOREIGN KEY (account_status_id)
                               REFERENCES account_statuses(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT fk_user_created_by
                           FOREIGN KEY (created_by)
                               REFERENCES users(id)
                               ON DELETE SET NULL,

                       CONSTRAINT chk_user_login_identifier
                           CHECK (
                               NULLIF(BTRIM(phone), '') IS NOT NULL
                                   OR NULLIF(BTRIM(email::TEXT), '') IS NOT NULL
                               ),

                       CONSTRAINT chk_user_phone_not_blank
                           CHECK (
                               phone IS NULL
                                   OR BTRIM(phone) <> ''
                               ),

                       CONSTRAINT chk_user_email_not_blank
                           CHECK (
                               email IS NULL
                                   OR BTRIM(email::TEXT) <> ''
                               ),

                       CONSTRAINT chk_user_password_hash_not_blank
                           CHECK (BTRIM(password_hash) <> ''),

                       CONSTRAINT chk_user_failed_login_count
                           CHECK (failed_login_count >= 0)
);

CREATE INDEX idx_users_role_id
    ON users(role_id);

CREATE INDEX idx_users_account_status_id
    ON users(account_status_id);

CREATE INDEX idx_users_created_by
    ON users(created_by);

CREATE INDEX idx_users_locked_until
    ON users(locked_until)
    WHERE locked_until IS NOT NULL;


--28. organization_profile
CREATE TABLE organization_profile (
                                      id SMALLINT PRIMARY KEY DEFAULT 1,

                                      name_km VARCHAR(255) NOT NULL,
                                      name_en VARCHAR(255),

                                      tagline_km VARCHAR(255),
                                      tagline_en VARCHAR(255),

                                      about_km TEXT,
                                      about_en TEXT,

                                      address TEXT,
                                      phone VARCHAR(30),
                                      email CITEXT,
                                      website VARCHAR(500),

                                      facebook_url VARCHAR(500),
                                      telegram_url VARCHAR(500),
                                      youtube_url VARCHAR(500),

                                      logo_file_id BIGINT,
                                      cover_file_id BIGINT,

                                      updated_by BIGINT,

                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                      CONSTRAINT chk_organization_single_row
                                          CHECK (id = 1),

                                      CONSTRAINT chk_organization_name_km_not_blank
                                          CHECK (BTRIM(name_km) <> ''),

                                      CONSTRAINT chk_organization_email_not_blank
                                          CHECK (
                                              email IS NULL
                                                  OR BTRIM(email::TEXT) <> ''
                                              ),

                                      CONSTRAINT fk_organization_logo
                                          FOREIGN KEY (logo_file_id)
                                              REFERENCES files(id)
                                              ON DELETE SET NULL,

                                      CONSTRAINT fk_organization_cover
                                          FOREIGN KEY (cover_file_id)
                                              REFERENCES files(id)
                                              ON DELETE SET NULL,

                                      CONSTRAINT fk_organization_updated_by
                                          FOREIGN KEY (updated_by)
                                              REFERENCES users(id)
                                              ON DELETE SET NULL

);
