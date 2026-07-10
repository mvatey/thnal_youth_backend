-- Enable UUID generation (only needs to run once per database)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Table users --
CREATE TABLE IF NOT EXISTS users (
                       user_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username        VARCHAR(50)  NOT NULL UNIQUE,
                       email           VARCHAR(255) NOT NULL UNIQUE,
                       phone           VARCHAR(20)  UNIQUE,
                       password_hash   VARCHAR(255) NOT NULL,
                       full_name       VARCHAR(150),
                       avatar_url      TEXT,
                       status          VARCHAR(20)  NOT NULL DEFAULT 'active'
                           CHECK (status IN ('active', 'inactive', 'suspended')),
                       last_login_at   TIMESTAMP,
                       created_by      UUID REFERENCES users(user_id),
                       created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- Member
CREATE TABLE members (
                         id SERIAL PRIMARY KEY,
                         member_no VARCHAR(50) UNIQUE NOT NULL,
                         branch_id BIGINT NOT NULL,
                         position_id BIGINT,
                         status_id BIGINT,
                         full_name_en VARCHAR(100),
                         full_name_kh VARCHAR(100),
                         gender VARCHAR(10),
                         date_of_birth DATE,
                         phone VARCHAR(20),
                         email VARCHAR(100),
                         address TEXT,
                         bio TEXT
);


CREATE TABLE member_positions (
                                  id SERIAL PRIMARY KEY,
                                  code VARCHAR(50) UNIQUE NOT NULL,
                                  label_km VARCHAR(100),
                                  label_en VARCHAR(100),
                                  description TEXT,
                                  is_active BOOLEAN,
                                  sort_order INT
);



CREATE TABLE member_statuses (
                                 id SERIAL PRIMARY KEY,
                                 code VARCHAR(50) UNIQUE NOT NULL,
                                 label_km VARCHAR(100),
                                 label_en VARCHAR(100),
                                 is_active BOOLEAN
);
CREATE TABLE branches (
                          id SERIAL PRIMARY KEY,
                          branch_code VARCHAR(50) UNIQUE NOT NULL,
                          name_en VARCHAR(100),
                          name_kh VARCHAR(100)
);




-- Family relation lookup

-- Lookup tables
CREATE TABLE IF NOT EXISTS public.member_positions (
                                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                       code VARCHAR(50) UNIQUE NOT NULL,
                                                       label_en TEXT NOT NULL,
                                                       label_kh TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS public.member_statuses (
                                                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                      code VARCHAR(50) UNIQUE NOT NULL,
                                                      label_en TEXT NOT NULL,
                                                      label_kh TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS public.family_relations (
                                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                       code VARCHAR(50) UNIQUE NOT NULL,
                                                       label_en TEXT NOT NULL,
                                                       label_kh TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS public.life_statuses (
                                                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                    code VARCHAR(50) UNIQUE NOT NULL,
                                                    label_en TEXT NOT NULL,
                                                    label_kh TEXT NOT NULL
);

-- Members table
CREATE TABLE IF NOT EXISTS public.members (
                                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              member_no VARCHAR(50) UNIQUE NOT NULL,
                                              full_name_en TEXT,
                                              full_name_kh TEXT,
                                              gender CHAR(1),
                                              date_of_birth DATE,
                                              branch_id BIGINT,
                                              status_id BIGINT REFERENCES public.member_statuses(id)
);

-- Member family table
CREATE TABLE IF NOT EXISTS public.member_family (
                                                    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                    member_id BIGINT NOT NULL REFERENCES public.members(id) ON DELETE CASCADE,
                                                    relation_id BIGINT NOT NULL REFERENCES public.family_relations(id),
                                                    name_kh TEXT,
                                                    name_en TEXT,
                                                    date_of_birth DATE,
                                                    occupation TEXT,
                                                    phone TEXT,
                                                    address TEXT,
                                                    life_status_id BIGINT REFERENCES public.life_statuses(id),
                                                    note TEXT,
                                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                                    UNIQUE (member_id, relation_id)
);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_member_family_updated
    BEFORE UPDATE ON public.member_family
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Seed data
INSERT INTO public.member_statuses (code, label_en, label_kh)
VALUES ('ACTIVE','Active','សកម្ម')
ON CONFLICT (code) DO NOTHING;

INSERT INTO public.family_relations (code, label_en, label_kh)
VALUES ('FATHER','Father','ឳពុក'),
       ('MOTHER','Mother','ម្តាយ'),
       ('SPOUSE','Spouse','ប្រពន្ធ')
ON CONFLICT (code) DO NOTHING;

INSERT INTO public.life_statuses (code, label_en, label_kh)
VALUES ('ALIVE','Alive','រស់'),
       ('DECEASED','Deceased','ស្លាប់')
ON CONFLICT (code) DO NOTHING;

-- Test member
INSERT INTO public.members (member_no, full_name_en, full_name_kh, gender, date_of_birth, branch_id, status_id)
VALUES ('M001','Sok San','សុខ សាន','M','1985-01-01',1,(SELECT id FROM public.member_statuses WHERE code='ACTIVE'))
ON CONFLICT (member_no) DO NOTHING;

-- Family records
INSERT INTO public.member_family (member_id, relation_id, name_kh, name_en, date_of_birth, occupation, phone, address, life_status_id, note)
VALUES
    (1, (SELECT id FROM public.family_relations WHERE code='FATHER'), 'ឳពុក សុខ សាន', 'Father Sok San', '1965-01-01', 'Farmer', '012345678', 'Siem Reap', (SELECT id FROM public.life_statuses WHERE code='ALIVE'), 'Healthy and supportive'),
    (1, (SELECT id FROM public.family_relations WHERE code='MOTHER'), 'ម្តាយ សុខ សាន', 'Mother Sok San', '1970-01-01', 'Homemaker', NULL, 'Siem Reap', (SELECT id FROM public.life_statuses WHERE code='DECEASED'), 'Passed away peacefully'),
    (1, (SELECT id FROM public.family_relations WHERE code='SPOUSE'), 'ប្រពន្ធ សុខ សាន', 'Spouse Sok San', '1988-01-01', 'Teacher', '098765432', 'Phnom Penh', (SELECT id FROM public.life_statuses WHERE code='ALIVE'), 'Supportive partner')
ON CONFLICT DO NOTHING;

-- Family records

CREATE TABLE member_family (
                               id SERIAL PRIMARY KEY,
                               member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                               relation VARCHAR(50) NOT NULL,
                               full_name_en VARCHAR(100),
                               full_name_kh VARCHAR(100),
                               phone VARCHAR(20),
                               email VARCHAR(100),
                               occupation VARCHAR(100)
);
-- member_work_history.sql
CREATE TABLE member_work_history (
                                     id SERIAL PRIMARY KEY,
                                     member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
                                     organization VARCHAR(150) NOT NULL,
                                     position_title VARCHAR(100) NOT NULL,
                                     start_date DATE,
                                     end_date DATE,
                                     description TEXT
);
