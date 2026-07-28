-- ============================================================
-- TNAL Youth Authentication Module
-- Spring Boot + JPA + JWT
-- PostgreSQL 15+
-- ============================================================

CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- CLEAN DATABASE - DEVELOPMENT ONLY
-- ============================================================

DROP TRIGGER IF EXISTS trg_users_audit ON users;
DROP TRIGGER IF EXISTS trg_users_updated ON users;

DROP FUNCTION IF EXISTS audit_trigger() CASCADE;
DROP FUNCTION IF EXISTS set_updated_at() CASCADE;

DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS login_history CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    phone VARCHAR(20) NOT NULL UNIQUE,
    email CITEXT UNIQUE,

    password_hash TEXT NOT NULL,

    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    full_name_km TEXT NOT NULL,
    full_name_en TEXT,
    profile_image TEXT,

    failed_login_count INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMPTZ,
    locked_until TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CHECK (phone ~ '^[0-9+() -]{6,20}$'),
    CHECK (role IN ('ADMIN', 'BRANCH_LEADER', 'SECRETARY', 'MEMBER')),
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- ============================================================
-- UPDATED_AT TRIGGER
-- ============================================================

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER
AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- PASSWORD RESET TOKENS
-- ============================================================

CREATE TABLE password_reset_tokens
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL
        REFERENCES users(id)
            ON DELETE CASCADE,

    otp_code_hash TEXT NOT NULL,

    delivery_channel VARCHAR(20) NOT NULL,

    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,

    attempts INT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CHECK (delivery_channel IN ('SMS', 'EMAIL'))
);

CREATE INDEX idx_password_reset_user
    ON password_reset_tokens(user_id);

CREATE INDEX idx_password_reset_active
    ON password_reset_tokens(user_id)
    WHERE consumed_at IS NULL;

-- ============================================================
-- REFRESH TOKENS
-- ============================================================

CREATE TABLE refresh_tokens
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL
        REFERENCES users(id)
            ON DELETE CASCADE,

    token UUID NOT NULL DEFAULT gen_random_uuid(),

    expires_at TIMESTAMPTZ NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_refresh_token
    ON refresh_tokens(token);

CREATE INDEX idx_refresh_user
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_valid
    ON refresh_tokens(token, revoked, expires_at);

-- ============================================================
-- LOGIN HISTORY
-- ============================================================

CREATE TABLE login_history
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT
                           REFERENCES users(id)
                               ON DELETE SET NULL,

    login_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    ip_address VARCHAR(50),

    device TEXT,

    browser TEXT,

    success BOOLEAN NOT NULL
);

CREATE INDEX idx_login_user
    ON login_history(user_id);

CREATE INDEX idx_login_time
    ON login_history(login_time DESC);

CREATE INDEX idx_login_success
    ON login_history(success);

-- ============================================================
-- AUDIT LOGS
-- ============================================================

CREATE TABLE audit_logs
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT
                           REFERENCES users(id)
                               ON DELETE SET NULL,

    action TEXT NOT NULL,

    entity TEXT NOT NULL,

    entity_id BIGINT,

    before_data JSONB,

    after_data JSONB,

    ip_address INET,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user
    ON audit_logs(user_id);

CREATE INDEX idx_audit_entity
    ON audit_logs(entity, entity_id);

CREATE INDEX idx_audit_created
    ON audit_logs(created_at DESC);

-- ============================================================
-- AUDIT TRIGGER
-- ============================================================

CREATE OR REPLACE FUNCTION audit_trigger()
    RETURNS TRIGGER
AS $$
DECLARE
    v_user_id BIGINT;
BEGIN
    BEGIN
        v_user_id :=
                NULLIF(current_setting('app.current_user_id', TRUE), '')::BIGINT;
    EXCEPTION
        WHEN OTHERS THEN
            v_user_id := NULL;
    END;

    IF TG_OP = 'INSERT' THEN

        INSERT INTO audit_logs
        (
            user_id,
            action,
            entity,
            entity_id,
            after_data
        )
        VALUES
            (
                v_user_id,
                'CREATE',
                TG_TABLE_NAME,
                NEW.id,
                to_jsonb(NEW)
            );

        RETURN NEW;

    ELSIF TG_OP = 'UPDATE' THEN

        INSERT INTO audit_logs
        (
            user_id,
            action,
            entity,
            entity_id,
            before_data,
            after_data
        )
        VALUES
            (
                v_user_id,
                'UPDATE',
                TG_TABLE_NAME,
                NEW.id,
                to_jsonb(OLD),
                to_jsonb(NEW)
            );

        RETURN NEW;

    ELSE

        INSERT INTO audit_logs
        (
            user_id,
            action,
            entity,
            entity_id,
            before_data
        )
        VALUES
            (
                v_user_id,
                'DELETE',
                TG_TABLE_NAME,
                OLD.id,
                to_jsonb(OLD)
            );

        RETURN OLD;

    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_audit
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW
EXECUTE FUNCTION audit_trigger();

-- ============================================================
-- SEED USERS
-- Password depends on your BCrypt hash.
-- Keep your current password test value if it already works.
-- ============================================================

INSERT INTO users
(
    phone,
    email,
    password_hash,
    role,
    status,
    full_name_km,
    full_name_en
)
VALUES
    (
        '081816685',
        'admin1@gmail.com',
        '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
        'ADMIN',
        'ACTIVE',
        'Admin',
        'System Administrator'
    );

SELECT * From users;