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

CREATE TABLE member_positions (
                                  id SERIAL PRIMARY KEY,
                                  code VARCHAR(50),
                                  label_km VARCHAR(100),
                                  label_en VARCHAR(100),
                                  description TEXT,
                                  is_active BOOLEAN,
                                  sort_order INT,
                                  created_at TIMESTAMP,
                                  updated_at TIMESTAMP
);

CREATE TABLE member_statuses (
                                 id SERIAL PRIMARY KEY,
                                 code VARCHAR(50),
                                 label_km VARCHAR(100),
                                 label_en VARCHAR(100),
                                 is_active BOOLEAN,
                                 sort_order INT,
                                 created_at TIMESTAMP,
                                 updated_at TIMESTAMP
);

CREATE TABLE members (
                         id SERIAL PRIMARY KEY,
                         member_no VARCHAR(50) UNIQUE NOT NULL,
                         user_id BIGINT,
                         branch_id BIGINT,
                         position_id BIGINT REFERENCES member_positions(id),
                         status_id BIGINT REFERENCES member_statuses(id),
                         full_name_km VARCHAR(100),
                         full_name_en VARCHAR(100),
                         gender VARCHAR(20),
                         date_of_birth DATE,
                         phone VARCHAR(20),
                         email VARCHAR(100),
                         address TEXT,
                         profile_photo VARCHAR(255),
                         cv_file VARCHAR(255),
                         joined_on DATE,
                         bio TEXT
);
