-- ============================================================
-- CYNA — Cambodia Youth Association Management System
-- Final schema. PostgreSQL 15+.
-- Conventions:
--   * BIGINT GENERATED ALWAYS AS IDENTITY PKs
--   * TIMESTAMPTZ in UTC; app converts to Asia/Phnom_Penh
--   * Money: NUMERIC(14,2) + currency CHAR(3) ('USD'|'KHR')
--   * No DELETE — deactivation only (status flip)
--   * Audit via triggers; app MUST set app.current_user_id per transaction
--   * Single tenant: 1 organization, 1 admin, N branches, N members per branch
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- ---------- helpers ----------
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- 1. GEOGRAPHY
-- ============================================================
CREATE TABLE provinces (
                           id          SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           code        TEXT NOT NULL UNIQUE,
                           name_km     TEXT NOT NULL UNIQUE,
                           name_en     TEXT NOT NULL,
                           is_active   BOOLEAN NOT NULL DEFAULT TRUE,
                           sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE districts (
                           id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           province_id  SMALLINT NOT NULL REFERENCES provinces(id) ON DELETE RESTRICT,
                           name_km      TEXT NOT NULL,
                           name_en      TEXT,
                           is_active    BOOLEAN NOT NULL DEFAULT TRUE,
                           UNIQUE (province_id, name_km)
);
CREATE INDEX idx_districts_province ON districts(province_id);

-- ============================================================
-- 2. LOOKUPS (admin-editable from Settings page)
-- ============================================================
CREATE TABLE member_positions (
                                  id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                  description TEXT, is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                  sort_order INT NOT NULL DEFAULT 0,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE branch_types (
                              id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                              description TEXT, is_active BOOLEAN NOT NULL DEFAULT TRUE,
                              sort_order INT NOT NULL DEFAULT 0,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE branch_statuses (
                                 id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                 is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE member_statuses (
                                 id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                 is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE program_types (
                               id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                               description TEXT, is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               sort_order INT NOT NULL DEFAULT 0,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE program_sectors (
                                 id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                 description TEXT, is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                 sort_order INT NOT NULL DEFAULT 0,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE program_statuses (
                                  id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                  is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE attendance_statuses (
                                     id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                     is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE document_types (
                                id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE payment_methods (
                                 id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                 is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE contribution_types (
                                    id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                    is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE education_levels (
                                  id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                  is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE notification_types (
                                    id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                                    is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE sponsor_types (
                               id SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               code TEXT NOT NULL UNIQUE, label_km TEXT NOT NULL, label_en TEXT,
                               is_active BOOLEAN NOT NULL DEFAULT TRUE, sort_order INT NOT NULL DEFAULT 0,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

DO $$ DECLARE t TEXT; BEGIN
    FOR t IN SELECT unnest(ARRAY[
        'member_positions','branch_types','branch_statuses','member_statuses',
        'program_types','program_sectors','program_statuses','attendance_statuses',
        'document_types','payment_methods','contribution_types','education_levels',
        'notification_types','sponsor_types'])
        LOOP
            EXECUTE format(
                    'CREATE TRIGGER trg_%I_updated BEFORE UPDATE ON %I
                     FOR EACH ROW EXECUTE FUNCTION set_updated_at()', t, t);
        END LOOP;
END $$;

-- ============================================================
-- 3. FILES (centralized storage metadata)
-- ============================================================
CREATE TABLE files (
                       id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       storage_key     TEXT NOT NULL UNIQUE,
                       url             TEXT NOT NULL,
                       original_name   TEXT,
                       mime_type       TEXT,
                       size_bytes      BIGINT CHECK (size_bytes IS NULL OR size_bytes >= 0),
                       width_px        INT,
                       height_px       INT,
                       uploaded_by     BIGINT,
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 4. USERS + AUTH (4 fixed roles; no RBAC tables)
-- ============================================================
CREATE TYPE user_account_status AS ENUM ('ACTIVE','INACTIVE');
CREATE TYPE system_role AS ENUM ('ADMIN');  -- only system-level role; SEC/HEAD/MEMBER flow from branch_staff + members

CREATE TABLE users (
                       id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       phone              TEXT NOT NULL UNIQUE,
                       email              CITEXT UNIQUE,
                       password_hash      TEXT NOT NULL,
                       system_role        system_role,
                       status             user_account_status NOT NULL DEFAULT 'ACTIVE',
                       full_name_km       TEXT NOT NULL,
                       full_name_en       TEXT,
                       avatar_file_id     BIGINT REFERENCES files(id) ON DELETE SET NULL,
                       last_login_at      TIMESTAMPTZ,
                       failed_login_count INT NOT NULL DEFAULT 0,
                       locked_until       TIMESTAMPTZ,
                       created_by         BIGINT REFERENCES users(id) ON DELETE SET NULL,
                       created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       CHECK (phone ~ '^[0-9+()\- ]{6,20}$')
);
CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE UNIQUE INDEX uq_only_one_admin ON users((system_role))
    WHERE system_role = 'ADMIN';

ALTER TABLE files
    ADD CONSTRAINT fk_files_uploader
        FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE TYPE otp_channel AS ENUM ('SMS','EMAIL');
CREATE TABLE password_reset_tokens (
                                       id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       otp_code_hash TEXT NOT NULL,
                                       delivery_channel otp_channel NOT NULL,
                                       expires_at    TIMESTAMPTZ NOT NULL,
                                       consumed_at   TIMESTAMPTZ,
                                       attempts      INT NOT NULL DEFAULT 0,
                                       created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_prt_user_active ON password_reset_tokens(user_id)
    WHERE consumed_at IS NULL;

-- ============================================================
-- 5. BRANCHES
-- ============================================================
CREATE TABLE branches (
                          id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          code               TEXT NOT NULL UNIQUE,
                          name_km            TEXT NOT NULL,
                          name_en            TEXT,
                          type_id            SMALLINT NOT NULL REFERENCES branch_types(id) ON DELETE RESTRICT,
                          status_id          SMALLINT NOT NULL REFERENCES branch_statuses(id) ON DELETE RESTRICT,
                          province_id        SMALLINT NOT NULL REFERENCES provinces(id) ON DELETE RESTRICT,
                          district_id        INT REFERENCES districts(id) ON DELETE RESTRICT,
                          address_line       TEXT,
                          google_map_url     TEXT,
                          phone              TEXT,
                          email              CITEXT,
                          founded_on         DATE,
                          cover_image_id     BIGINT REFERENCES files(id) ON DELETE SET NULL,
                          description        TEXT,
                          next_member_seq    INT NOT NULL DEFAULT 1,
                          created_by         BIGINT REFERENCES users(id) ON DELETE SET NULL,
                          created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_branches_province ON branches(province_id);
CREATE INDEX idx_branches_district ON branches(district_id);
CREATE INDEX idx_branches_status   ON branches(status_id);
CREATE INDEX idx_branches_name_km  ON branches(name_km);
CREATE INDEX idx_branches_name_en  ON branches(name_en);
CREATE TRIGGER trg_branches_updated BEFORE UPDATE ON branches
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 6. MEMBERS + sub-profiles
-- ============================================================
CREATE TYPE gender AS ENUM ('MALE','FEMALE','OTHER');

CREATE TABLE members (
                         id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         member_no         TEXT NOT NULL UNIQUE
                             CHECK (member_no ~ '^[A-Z]+-[0-9]{4,}$'),
                         user_id           BIGINT UNIQUE REFERENCES users(id) ON DELETE SET NULL,
                         branch_id         BIGINT NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
                         position_id       SMALLINT REFERENCES member_positions(id) ON DELETE SET NULL,
                         status_id         SMALLINT NOT NULL REFERENCES member_statuses(id) ON DELETE RESTRICT,
                         full_name_km      TEXT NOT NULL,
                         full_name_en      TEXT,
                         gender            gender NOT NULL,
                         date_of_birth     DATE,
                         place_of_birth    TEXT,
                         phone             TEXT NOT NULL,
                         email             CITEXT,
                         current_address   TEXT,
                         permanent_address TEXT,
                         profile_photo_id  BIGINT REFERENCES files(id) ON DELETE SET NULL,
                         cv_file_id        BIGINT REFERENCES files(id) ON DELETE SET NULL,
                         joined_on         DATE NOT NULL,
                         bio               TEXT,
                         created_by        BIGINT REFERENCES users(id) ON DELETE SET NULL,
                         created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_members_branch   ON members(branch_id);
CREATE INDEX idx_members_position ON members(position_id);
CREATE INDEX idx_members_status   ON members(status_id);
CREATE INDEX idx_members_name_km  ON members(full_name_km);
CREATE INDEX idx_members_name_en  ON members(full_name_en);
CREATE INDEX idx_members_phone    ON members(phone);
CREATE TRIGGER trg_members_updated BEFORE UPDATE ON members
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TYPE family_relation AS ENUM ('SPOUSE','FATHER','MOTHER');
CREATE TYPE life_status    AS ENUM ('ALIVE','DECEASED');
CREATE TABLE member_family (
                               id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               member_id     BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                               relation      family_relation NOT NULL,
                               name_km       TEXT, name_en TEXT,
                               date_of_birth DATE, occupation TEXT, phone TEXT, address TEXT,
                               life_status   life_status, note TEXT,
                               created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               UNIQUE (member_id, relation)
);
CREATE TRIGGER trg_member_family_updated BEFORE UPDATE ON member_family
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE member_work_history (
                                     id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     member_id       BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                                     organization    TEXT NOT NULL,
                                     position_title  TEXT, address TEXT, appointment_no TEXT,
                                     start_date      DATE, end_date DATE,
                                     is_current      BOOLEAN NOT NULL DEFAULT FALSE,
                                     note            TEXT,
                                     created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);
CREATE INDEX idx_mwh_member ON member_work_history(member_id);
CREATE TRIGGER trg_mwh_updated BEFORE UPDATE ON member_work_history
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE member_education (
                                  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  member_id       BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                                  school_name     TEXT NOT NULL,
                                  level_id        SMALLINT REFERENCES education_levels(id) ON DELETE SET NULL,
                                  field_of_study  TEXT, country TEXT, province TEXT,
                                  start_date      DATE, end_date DATE,
                                  is_current      BOOLEAN NOT NULL DEFAULT FALSE,
                                  note            TEXT,
                                  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);
CREATE INDEX idx_medu_member ON member_education(member_id);
CREATE TRIGGER trg_medu_updated BEFORE UPDATE ON member_education
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE member_skills (
                               id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               member_id   BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                               skill_name  TEXT NOT NULL,
                               note        TEXT,
                               created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               UNIQUE (member_id, skill_name)
);
CREATE INDEX idx_mskills_member ON member_skills(member_id);

CREATE TYPE language_proficiency AS ENUM ('BEGINNER','INTERMEDIATE','ADVANCED','FLUENT','NATIVE');
CREATE TABLE member_languages (
                                  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  member_id   BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                                  language    TEXT NOT NULL,
                                  listening   language_proficiency,
                                  speaking    language_proficiency,
                                  reading     language_proficiency,
                                  writing     language_proficiency,
                                  UNIQUE (member_id, language)
);
CREATE INDEX idx_mlang_member ON member_languages(member_id);

CREATE TABLE member_political_affiliations (
                                               id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                               member_id       BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                                               organization    TEXT NOT NULL,
                                               country         TEXT, location TEXT,
                                               position_title  TEXT, decree_no TEXT,
                                               start_date      DATE, end_date DATE,
                                               is_current      BOOLEAN NOT NULL DEFAULT FALSE,
                                               note            TEXT,
                                               created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                               updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                               CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);
CREATE INDEX idx_mpol_member ON member_political_affiliations(member_id);
CREATE TRIGGER trg_mpol_updated BEFORE UPDATE ON member_political_affiliations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TYPE credential_kind AS ENUM (
    'ID_CARD','APPOINTMENT_LETTER','MISSION_LETTER','CERTIFICATE','OTHER'
    );

-- [FIX BLOCKER]: program_id is a forward reference. Declare column WITHOUT the FK here;
-- the FK is added below AFTER `programs` is created.
CREATE TABLE member_credentials (
                                    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    member_id     BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                                    kind          credential_kind NOT NULL,
                                    program_id    BIGINT,   -- FK added post-programs
                                    serial_no     TEXT, title TEXT,
                                    issued_on     DATE NOT NULL,
                                    expires_on    DATE,
                                    issued_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                    file_id       BIGINT REFERENCES files(id) ON DELETE SET NULL,
                                    is_revoked    BOOLEAN NOT NULL DEFAULT FALSE,
                                    revoked_at    TIMESTAMPTZ,
                                    revoke_reason TEXT, note TEXT,
                                    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    CHECK (expires_on IS NULL OR expires_on >= issued_on),
                                    CHECK (
                                        (kind = 'CERTIFICATE' AND program_id IS NOT NULL)
                                            OR (kind <> 'CERTIFICATE')
                                        ),
                                    UNIQUE (member_id, program_id, kind)
);
CREATE INDEX idx_mcred_member  ON member_credentials(member_id);
CREATE INDEX idx_mcred_kind    ON member_credentials(kind);
CREATE INDEX idx_mcred_program ON member_credentials(program_id);
CREATE UNIQUE INDEX uq_mcred_serial ON member_credentials(kind, serial_no)
    WHERE serial_no IS NOT NULL AND is_revoked = FALSE;
CREATE TRIGGER trg_mcred_updated BEFORE UPDATE ON member_credentials
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 7. BRANCH STAFF (history of branch leaders + secretaries)
-- ============================================================
CREATE TYPE branch_role AS ENUM ('BRANCH_LEADER','SECRETARY');
CREATE TABLE branch_staff (
                              id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              branch_id             BIGINT NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
                              member_id             BIGINT NOT NULL REFERENCES members(id) ON DELETE RESTRICT,
                              role                  branch_role NOT NULL,
                              started_on            DATE NOT NULL,
                              ended_on              DATE,
                              appointment_letter_id BIGINT REFERENCES files(id) ON DELETE SET NULL,
                              note                  TEXT,
                              created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              CHECK (ended_on IS NULL OR ended_on >= started_on)
);
CREATE INDEX idx_bs_branch ON branch_staff(branch_id);
CREATE INDEX idx_bs_member ON branch_staff(member_id);
CREATE UNIQUE INDEX uq_bs_active ON branch_staff(branch_id, role)
    WHERE ended_on IS NULL;
CREATE TRIGGER trg_bs_updated BEFORE UPDATE ON branch_staff
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 8. PROGRAMS
-- ============================================================
CREATE TABLE programs (
                          id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          code                TEXT UNIQUE,
                          title_km            TEXT NOT NULL,
                          title_en            TEXT,
                          description         TEXT,
                          branch_id           BIGINT NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
                          type_id             SMALLINT NOT NULL REFERENCES program_types(id) ON DELETE RESTRICT,
                          sector_id           SMALLINT NOT NULL REFERENCES program_sectors(id) ON DELETE RESTRICT,
                          status_id           SMALLINT NOT NULL REFERENCES program_statuses(id) ON DELETE RESTRICT,
                          location_name       TEXT,
                          province_id         SMALLINT REFERENCES provinces(id),
                          district_id         INT REFERENCES districts(id),
                          address_line        TEXT,
                          google_map_url      TEXT,
                          starts_at           TIMESTAMPTZ NOT NULL,
                          ends_at             TIMESTAMPTZ NOT NULL,
                          capacity            INT CHECK (capacity IS NULL OR capacity >= 0),
                          manager_member_id   BIGINT REFERENCES members(id) ON DELETE SET NULL,
                          manager_phone       TEXT,
                          manager_email       CITEXT,
                          budget_total        NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (budget_total >= 0),
                          budget_currency     CHAR(3) NOT NULL DEFAULT 'USD' CHECK (budget_currency IN ('USD','KHR')),
                          cover_image_id      BIGINT REFERENCES files(id) ON DELETE SET NULL,
                          is_public           BOOLEAN NOT NULL DEFAULT FALSE,
                          is_frozen           BOOLEAN NOT NULL DEFAULT FALSE,
                          created_by          BIGINT REFERENCES users(id) ON DELETE SET NULL,
                          created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          CHECK (ends_at >= starts_at)
);
CREATE INDEX idx_programs_branch    ON programs(branch_id);
CREATE INDEX idx_programs_status    ON programs(status_id);
CREATE INDEX idx_programs_sector    ON programs(sector_id);
CREATE INDEX idx_programs_starts_at ON programs(starts_at);
CREATE INDEX idx_programs_public    ON programs(is_public, starts_at)
    WHERE is_public = TRUE AND is_frozen = FALSE;
CREATE INDEX idx_programs_title_km  ON programs(title_km);
CREATE INDEX idx_programs_title_en  ON programs(title_en);
CREATE TRIGGER trg_programs_updated BEFORE UPDATE ON programs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- [FIX BLOCKER]: now that `programs` exists, add the forward FK
ALTER TABLE member_credentials
    ADD CONSTRAINT member_credentials_program_id_fkey
        FOREIGN KEY (program_id) REFERENCES programs(id) ON DELETE SET NULL;

CREATE TABLE program_participants (
                                      id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      program_id      BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
                                      member_id       BIGINT REFERENCES members(id) ON DELETE RESTRICT,
                                      guest_name      TEXT, guest_phone TEXT, guest_org TEXT,
                                      attendance_id   SMALLINT NOT NULL REFERENCES attendance_statuses(id) ON DELETE RESTRICT,
                                      registered_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      checked_in_at   TIMESTAMPTZ,
                                      note            TEXT,
                                      CHECK (member_id IS NOT NULL OR guest_name IS NOT NULL)
);
CREATE UNIQUE INDEX uq_pp_member ON program_participants(program_id, member_id)
    WHERE member_id IS NOT NULL;
CREATE INDEX idx_pp_program ON program_participants(program_id);
CREATE INDEX idx_pp_member  ON program_participants(member_id);

CREATE OR REPLACE FUNCTION enforce_participant_same_branch() RETURNS TRIGGER AS $$
DECLARE v_member_branch BIGINT; v_program_branch BIGINT;
BEGIN
    IF NEW.member_id IS NULL THEN
        RETURN NEW;
    END IF;
    SELECT branch_id INTO v_member_branch FROM members  WHERE id = NEW.member_id;
    SELECT branch_id INTO v_program_branch FROM programs WHERE id = NEW.program_id;
    IF v_member_branch IS DISTINCT FROM v_program_branch THEN
        RAISE EXCEPTION USING
            MESSAGE = format(
                    'Member %s (branch %s) cannot register for program %s (branch %s): cross-branch registration not allowed',
                    NEW.member_id, v_member_branch, NEW.program_id, v_program_branch),
            ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pp_same_branch
    BEFORE INSERT OR UPDATE OF member_id, program_id ON program_participants
    FOR EACH ROW EXECUTE FUNCTION enforce_participant_same_branch();

CREATE TABLE program_expenses (
                                  id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  program_id        BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
                                  title             TEXT NOT NULL,
                                  description       TEXT,
                                  quantity          NUMERIC(10,2) NOT NULL DEFAULT 1 CHECK (quantity >= 0),
                                  unit_price        NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (unit_price >= 0),
                                  currency          CHAR(3) NOT NULL CHECK (currency IN ('USD','KHR')),
                                  total_amount      NUMERIC(14,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
                                  spent_on          DATE,
                                  receipt_file_id   BIGINT REFERENCES files(id) ON DELETE SET NULL,
                                  recorded_by       BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                  created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pe_program ON program_expenses(program_id);

CREATE TABLE program_incomes (
                                 id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 program_id      BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
                                 source          TEXT NOT NULL,
                                 description     TEXT,
                                 amount          NUMERIC(14,2) NOT NULL CHECK (amount >= 0),
                                 currency        CHAR(3) NOT NULL CHECK (currency IN ('USD','KHR')),
                                 received_on     DATE,
                                 receipt_file_id BIGINT REFERENCES files(id) ON DELETE SET NULL,
                                 recorded_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                 created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pi_program ON program_incomes(program_id);

CREATE TABLE program_photos (
                                id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                program_id  BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
                                file_id     BIGINT NOT NULL REFERENCES files(id) ON DELETE CASCADE,
                                caption     TEXT,
                                sort_order  INT NOT NULL DEFAULT 0,
                                uploaded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pphotos_program ON program_photos(program_id, sort_order);

-- ============================================================
-- 9. SPONSORS
-- ============================================================
CREATE TABLE sponsors (
                          id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          name         TEXT NOT NULL,
                          type_id      SMALLINT REFERENCES sponsor_types(id) ON DELETE SET NULL,
                          phone        TEXT,
                          email        CITEXT,
                          address      TEXT,
                          website      TEXT,
                          logo_file_id BIGINT REFERENCES files(id) ON DELETE SET NULL,
                          note         TEXT,
                          is_active    BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sponsors_name ON sponsors(name);
CREATE TRIGGER trg_sponsors_updated BEFORE UPDATE ON sponsors
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 10. CONTRIBUTIONS (from members OR sponsors)
-- ============================================================
CREATE TABLE contributions (
                               id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               type_id             SMALLINT NOT NULL REFERENCES contribution_types(id) ON DELETE RESTRICT,
                               payment_method_id   SMALLINT REFERENCES payment_methods(id) ON DELETE SET NULL,
                               member_id           BIGINT REFERENCES members(id) ON DELETE SET NULL,
                               sponsor_id          BIGINT REFERENCES sponsors(id) ON DELETE SET NULL,
                               branch_id           BIGINT REFERENCES branches(id) ON DELETE RESTRICT,
                               program_id          BIGINT REFERENCES programs(id) ON DELETE SET NULL,
                               amount              NUMERIC(14,2) NOT NULL CHECK (amount > 0),
                               currency            CHAR(3) NOT NULL CHECK (currency IN ('USD','KHR')),
                               period_month        DATE,
                               paid_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               payment_reference   TEXT,
                               receipt_file_id     BIGINT REFERENCES files(id) ON DELETE SET NULL,
                               recorded_by         BIGINT REFERENCES users(id) ON DELETE SET NULL,
                               note                TEXT,
                               created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               CHECK (member_id IS NOT NULL OR sponsor_id IS NOT NULL),
                               CHECK (period_month IS NULL OR EXTRACT(DAY FROM period_month) = 1)
);
CREATE INDEX idx_contrib_member   ON contributions(member_id);
CREATE INDEX idx_contrib_sponsor  ON contributions(sponsor_id);
CREATE INDEX idx_contrib_branch   ON contributions(branch_id);
CREATE INDEX idx_contrib_program  ON contributions(program_id);
CREATE INDEX idx_contrib_paid_at  ON contributions(paid_at);
CREATE INDEX idx_contrib_period   ON contributions(period_month);
CREATE TRIGGER trg_contrib_updated BEFORE UPDATE ON contributions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 11. DOCUMENTS (one parent: branch OR member OR program)
-- ============================================================
CREATE TABLE documents (
                           id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           type_id       SMALLINT REFERENCES document_types(id) ON DELETE SET NULL,
                           file_id       BIGINT NOT NULL REFERENCES files(id) ON DELETE RESTRICT,
                           title         TEXT NOT NULL,
                           description   TEXT,
                           branch_id     BIGINT REFERENCES branches(id) ON DELETE RESTRICT,
                           member_id     BIGINT REFERENCES members(id) ON DELETE RESTRICT,
                           program_id    BIGINT REFERENCES programs(id) ON DELETE RESTRICT,
                           uploaded_by   BIGINT REFERENCES users(id) ON DELETE SET NULL,
                           created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           CHECK (
                               (branch_id IS NOT NULL)::INT
                                   + (member_id IS NOT NULL)::INT
                                   + (program_id IS NOT NULL)::INT = 1
                               )
);
CREATE INDEX idx_docs_branch  ON documents(branch_id);
CREATE INDEX idx_docs_member  ON documents(member_id);
CREATE INDEX idx_docs_program ON documents(program_id);
CREATE INDEX idx_docs_type    ON documents(type_id);
CREATE TRIGGER trg_docs_updated BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 12. NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
                               id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               type_id          SMALLINT NOT NULL REFERENCES notification_types(id) ON DELETE RESTRICT,
                               title            TEXT NOT NULL,
                               body             TEXT NOT NULL,
                               link_url         TEXT,
                               program_id       BIGINT REFERENCES programs(id) ON DELETE SET NULL,
                               branch_id        BIGINT REFERENCES branches(id) ON DELETE SET NULL,
                               sent_via_in_app  BOOLEAN NOT NULL DEFAULT TRUE,
                               sent_via_sms     BOOLEAN NOT NULL DEFAULT TRUE,
                               sent_via_email   BOOLEAN NOT NULL DEFAULT FALSE,
                               created_by       BIGINT REFERENCES users(id) ON DELETE SET NULL,
                               created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notif_type    ON notifications(type_id);
CREATE INDEX idx_notif_created ON notifications(created_at DESC);

CREATE TABLE notification_recipients (
                                         id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
                                         user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                         read_at         TIMESTAMPTZ,
                                         UNIQUE (notification_id, user_id)
);
CREATE INDEX idx_nr_user_unread ON notification_recipients(user_id)
    WHERE read_at IS NULL;

-- ============================================================
-- 13. PUBLIC SITE CMS
-- ============================================================
CREATE TABLE organization_profile (
                                      id            INT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
                                      name_km       TEXT NOT NULL,
                                      name_en       TEXT,
                                      tagline_km    TEXT,
                                      tagline_en    TEXT,
                                      about_km      TEXT,
                                      about_en      TEXT,
                                      address       TEXT,
                                      phone         TEXT,
                                      email         CITEXT,
                                      website       TEXT,
                                      facebook_url  TEXT,
                                      logo_file_id  BIGINT REFERENCES files(id) ON DELETE SET NULL,
                                      cover_file_id BIGINT REFERENCES files(id) ON DELETE SET NULL,
                                      updated_by    BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                      created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TRIGGER trg_org_profile_updated BEFORE UPDATE ON organization_profile
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE site_pages (
                            id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            slug         TEXT NOT NULL UNIQUE,
                            title_km     TEXT NOT NULL,
                            title_en     TEXT,
                            body_km      TEXT NOT NULL,
                            body_en      TEXT,
                            is_published BOOLEAN NOT NULL DEFAULT TRUE,
                            sort_order   INT NOT NULL DEFAULT 0,
                            updated_by   BIGINT REFERENCES users(id) ON DELETE SET NULL,
                            created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_site_pages_published ON site_pages(is_published, sort_order);
CREATE TRIGGER trg_site_pages_updated BEFORE UPDATE ON site_pages
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 14. AUDIT LOGS
-- ============================================================
CREATE TABLE audit_logs (
                            id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            user_id     BIGINT REFERENCES users(id) ON DELETE SET NULL,
                            action      TEXT NOT NULL,
                            entity      TEXT NOT NULL,
                            entity_id   BIGINT,
                            before_data JSONB,
                            after_data  JSONB,
                            ip_address  INET,
                            created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_user    ON audit_logs(user_id);
CREATE INDEX idx_audit_entity  ON audit_logs(entity, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);

CREATE OR REPLACE FUNCTION audit_trigger() RETURNS TRIGGER AS $$
DECLARE v_user_id BIGINT; v_ip INET;
BEGIN
    BEGIN
        v_user_id := NULLIF(current_setting('app.current_user_id', TRUE), '')::BIGINT;
    EXCEPTION WHEN OTHERS THEN v_user_id := NULL;
    END;
    BEGIN
        v_ip := NULLIF(current_setting('app.client_ip', TRUE), '')::INET;
    EXCEPTION WHEN OTHERS THEN v_ip := NULL;
    END;

    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_logs(user_id, action, entity, entity_id, after_data, ip_address)
        VALUES (v_user_id, 'CREATE', TG_TABLE_NAME, NEW.id, to_jsonb(NEW), v_ip);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        IF to_jsonb(OLD) IS DISTINCT FROM to_jsonb(NEW) THEN
            INSERT INTO audit_logs(user_id, action, entity, entity_id, before_data, after_data, ip_address)
            VALUES (v_user_id, 'UPDATE', TG_TABLE_NAME, NEW.id, to_jsonb(OLD), to_jsonb(NEW), v_ip);
        END IF;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit_logs(user_id, action, entity, entity_id, before_data, ip_address)
        VALUES (v_user_id, 'DELETE', TG_TABLE_NAME, OLD.id, to_jsonb(OLD), v_ip);
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

DO $$ DECLARE t TEXT; BEGIN
    FOR t IN SELECT unnest(ARRAY[
        'users','branches','members','member_family','member_work_history',
        'member_education','member_skills','member_languages',
        'member_political_affiliations','member_credentials','branch_staff',
        'programs','program_participants','program_expenses','program_incomes',
        'program_photos','sponsors','contributions','documents',
        'organization_profile','site_pages'])
        LOOP
            EXECUTE format(
                    'CREATE TRIGGER trg_%I_audit
                     AFTER INSERT OR UPDATE OR DELETE ON %I
                     FOR EACH ROW EXECUTE FUNCTION audit_trigger()', t, t);
        END LOOP;
END $$;

-- ============================================================
-- 15. STATUS CASCADE (branch deactivation → members + programs)
-- ============================================================
CREATE OR REPLACE FUNCTION cascade_branch_status() RETURNS TRIGGER AS $$
DECLARE v_inactive_branch SMALLINT; v_inactive_member SMALLINT;
BEGIN
    SELECT id INTO v_inactive_branch FROM branch_statuses WHERE code = 'INACTIVE';
    SELECT id INTO v_inactive_member FROM member_statuses WHERE code = 'INACTIVE';
    IF v_inactive_branch IS NULL OR v_inactive_member IS NULL THEN
        RAISE EXCEPTION 'cascade_branch_status: INACTIVE lookup rows missing (branch_statuses or member_statuses)';
    END IF;
    IF NEW.status_id = v_inactive_branch AND OLD.status_id IS DISTINCT FROM NEW.status_id THEN
        UPDATE members
        SET status_id = v_inactive_member
        WHERE branch_id = NEW.id
          AND status_id IS DISTINCT FROM v_inactive_member;

        UPDATE programs
        SET is_frozen = TRUE, is_public = FALSE
        WHERE branch_id = NEW.id AND is_frozen = FALSE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_branch_cascade
    AFTER UPDATE OF status_id ON branches
    FOR EACH ROW EXECUTE FUNCTION cascade_branch_status();

-- ============================================================
-- 16. AUDIT RETENTION (12 months) — call from scheduled job
-- ============================================================
CREATE OR REPLACE FUNCTION purge_old_audit_logs() RETURNS INT AS $$
DECLARE n INT;
BEGIN
    DELETE FROM audit_logs WHERE created_at < NOW() - INTERVAL '12 months';
    GET DIAGNOSTICS n = ROW_COUNT;
    RETURN n;
END;
$$ LANGUAGE plpgsql;