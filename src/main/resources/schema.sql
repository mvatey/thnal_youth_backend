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