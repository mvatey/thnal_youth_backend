-- ============================================================
-- V14 - Align Authentication With Final Design
-- PostgreSQL 15+
--
-- Purpose:
--   Align the team Flyway schema with the completed Authentication module.
--
-- Final Authentication fields used by Spring Boot:
--   users.phone
--   users.email
--   users.password_hash
--   users.role
--   users.status
--   users.full_name_km
--   users.full_name_en
--   users.profile_image
--   users.failed_login_count
--   users.last_login_at
--   users.locked_until
--   users.created_at
--   users.updated_at
--
-- Team-integration fields intentionally retained:
--   users.member_id
--   users.branch_id
--   users.created_by
--
-- IMPORTANT:
--   Do not edit V1-V13 after they have been applied.
--   Add this file as the next Flyway migration.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. USERS - add final Authentication columns
-- ============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(50),
    ADD COLUMN IF NOT EXISTS status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS full_name_km TEXT,
    ADD COLUMN IF NOT EXISTS full_name_en TEXT,
    ADD COLUMN IF NOT EXISTS profile_image TEXT,
    ADD COLUMN IF NOT EXISTS branch_id BIGINT;

-- Copy role code from the team lookup-based model.
UPDATE users u
SET role = r.code
FROM roles r
WHERE u.role_id = r.id
  AND u.role IS NULL;

-- Copy account status and translate SUSPENDED to LOCKED,
-- because the completed Authentication module uses LOCKED.
UPDATE users u
SET status = CASE
                 WHEN s.code = 'SUSPENDED' THEN 'LOCKED'
                 ELSE s.code
             END
FROM account_statuses s
WHERE u.account_status_id = s.id
  AND u.status IS NULL;

-- Copy profile information from the linked member when available.
UPDATE users u
SET full_name_km = COALESCE(u.full_name_km, m.full_name_km),
    full_name_en = COALESCE(u.full_name_en, m.full_name_en),
    branch_id    = COALESCE(u.branch_id, m.branch_id)
FROM members m
WHERE u.member_id = m.id;

-- profile_image remains TEXT in Authentication.
-- When a member already has a profile file, store its path as the initial value.
UPDATE users u
SET profile_image = f.file_path
FROM members m
JOIN files f ON f.id = m.profile_photo_id
WHERE u.member_id = m.id
  AND u.profile_image IS NULL;

-- Ensure required final values exist before adding NOT NULL constraints.
-- This block gives a clear error instead of a vague ALTER TABLE failure.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE phone IS NULL OR BTRIM(phone) = '') THEN
        RAISE EXCEPTION
            'V14 cannot continue: every users.phone must have a value. Fix users with NULL/blank phone first.';
    END IF;

    IF EXISTS (SELECT 1 FROM users WHERE role IS NULL) THEN
        RAISE EXCEPTION
            'V14 cannot continue: some users could not be mapped to a role.';
    END IF;

    IF EXISTS (SELECT 1 FROM users WHERE status IS NULL) THEN
        RAISE EXCEPTION
            'V14 cannot continue: some users could not be mapped to an account status.';
    END IF;
END;
$$;

-- If a user is not linked to a member, create a safe display name from
-- existing login information so full_name_km can become NOT NULL.
UPDATE users
SET full_name_km = COALESCE(
        NULLIF(BTRIM(full_name_km), ''),
        NULLIF(BTRIM(email::TEXT), ''),
        phone
    )
WHERE full_name_km IS NULL OR BTRIM(full_name_km) = '';

ALTER TABLE users
    ALTER COLUMN phone TYPE VARCHAR(20),
    ALTER COLUMN phone SET NOT NULL,
    ALTER COLUMN role SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'ACTIVE',
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN full_name_km SET NOT NULL;

-- Remove old lookup constraints and columns only after data migration.
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS fk_user_role,
    DROP CONSTRAINT IF EXISTS fk_user_account_status;

ALTER TABLE users
    DROP COLUMN IF EXISTS role_id,
    DROP COLUMN IF EXISTS account_status_id;

-- Add branch relationship required by the integrated project.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_users_branch'
          AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT fk_users_branch
            FOREIGN KEY (branch_id)
            REFERENCES branches(id)
            ON DELETE SET NULL;
    END IF;
END;
$$;

-- Replace old Authentication checks with final checks.
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_phone,
    DROP CONSTRAINT IF EXISTS chk_users_role,
    DROP CONSTRAINT IF EXISTS chk_users_status,
    DROP CONSTRAINT IF EXISTS chk_user_phone_not_blank,
    DROP CONSTRAINT IF EXISTS chk_user_login_identifier;

ALTER TABLE users
    ADD CONSTRAINT chk_users_phone
        CHECK (phone ~ '^[0-9+() -]{6,20}$'),
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'BRANCH_LEADER', 'SECRETARY', 'MEMBER')),
    ADD CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'));

DROP INDEX IF EXISTS idx_users_role_id;
DROP INDEX IF EXISTS idx_users_account_status_id;

CREATE INDEX IF NOT EXISTS idx_users_phone
    ON users(phone);

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users(email);

CREATE INDEX IF NOT EXISTS idx_users_role
    ON users(role);

CREATE INDEX IF NOT EXISTS idx_users_status
    ON users(status);

CREATE INDEX IF NOT EXISTS idx_users_branch
    ON users(branch_id);

-- ============================================================
-- 2. UPDATED_AT helper and users trigger
-- ============================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER
AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_updated ON users;

CREATE TRIGGER trg_users_updated
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- 3. REFRESH TOKENS - match completed Authentication
-- ============================================================

ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS revoked BOOLEAN NOT NULL DEFAULT FALSE;

-- Preserve existing revocation information from the team schema.
UPDATE refresh_tokens
SET revoked = TRUE
WHERE revoked_at IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_token
    ON refresh_tokens(token);

CREATE INDEX IF NOT EXISTS idx_refresh_user
    ON refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_valid
    ON refresh_tokens(token, revoked, expires_at);

-- revoked_at is retained for compatibility/history.
-- The completed Authentication code uses the revoked BOOLEAN column.

-- ============================================================
-- 4. PASSWORD RESET TOKENS - align indexes and constraints
-- ============================================================

ALTER TABLE password_reset_tokens
    ALTER COLUMN attempts SET DEFAULT 0,
    ALTER COLUMN created_at SET DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_password_reset_user
    ON password_reset_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_password_reset_active
    ON password_reset_tokens(user_id)
    WHERE consumed_at IS NULL;

-- ============================================================
-- 5. LOGIN HISTORY - add completed Authentication columns
-- ============================================================

ALTER TABLE login_history
    ADD COLUMN IF NOT EXISTS login_time TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS device TEXT,
    ADD COLUMN IF NOT EXISTS browser TEXT;

UPDATE login_history
SET login_time = COALESCE(login_time, created_at);

ALTER TABLE login_history
    ALTER COLUMN login_time SET DEFAULT NOW(),
    ALTER COLUMN login_time SET NOT NULL;

-- The completed entity maps ip_address as String.
ALTER TABLE login_history
    ALTER COLUMN ip_address TYPE VARCHAR(50)
    USING ip_address::TEXT;

CREATE INDEX IF NOT EXISTS idx_login_user
    ON login_history(user_id);

CREATE INDEX IF NOT EXISTS idx_login_time
    ON login_history(login_time DESC);

CREATE INDEX IF NOT EXISTS idx_login_success
    ON login_history(success);

-- Existing team columns such as login_identifier, failure_reason,
-- user_agent, and created_at are intentionally retained because they
-- are useful and do not break the completed Authentication entity.

-- ============================================================
-- 6. AUDIT TRIGGER - match completed Authentication behavior
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
        IF to_jsonb(OLD) IS DISTINCT FROM to_jsonb(NEW) THEN
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
        END IF;

        RETURN NEW;

    ELSIF TG_OP = 'DELETE' THEN
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

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_audit ON users;

CREATE TRIGGER trg_users_audit
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW
EXECUTE FUNCTION audit_trigger();

-- ============================================================
-- 7. FINAL VALIDATION
-- ============================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM users
        WHERE role NOT IN ('ADMIN', 'BRANCH_LEADER', 'SECRETARY', 'MEMBER')
    ) THEN
        RAISE EXCEPTION 'V14 validation failed: invalid users.role value found.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM users
        WHERE status NOT IN ('ACTIVE', 'INACTIVE', 'LOCKED')
    ) THEN
        RAISE EXCEPTION 'V14 validation failed: invalid users.status value found.';
    END IF;
END;
$$;
