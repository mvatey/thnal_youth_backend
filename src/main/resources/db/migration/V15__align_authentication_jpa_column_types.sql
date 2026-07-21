-- ============================================================
-- V15 - Align Authentication JPA Column Types
-- PostgreSQL 15+
--
-- Purpose:
--   Match PostgreSQL column types with the current JPA entities.
--
-- Important:
--   V14 has already been applied. Do not edit V14.
--   Add this file as the next Flyway migration.
-- ============================================================

-- ============================================================
-- 1. USERS.EMAIL
-- Java entity uses String, so Hibernate expects VARCHAR.
-- Preserve case-insensitive uniqueness with a LOWER(email) index.
-- ============================================================

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_email_key;

ALTER TABLE users
    ALTER COLUMN email TYPE VARCHAR(255)
    USING email::TEXT;

DROP INDEX IF EXISTS idx_users_email;

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users(email);

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_email_lower
    ON users(LOWER(email))
    WHERE email IS NOT NULL;

-- ============================================================
-- 2. AUDIT_LOGS.IP_ADDRESS
-- AuditLog.java maps ipAddress as String.
-- Convert PostgreSQL INET to VARCHAR without deleting audit rows.
-- ============================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'audit_logs'
          AND column_name = 'ip_address'
          AND udt_name = 'inet'
    ) THEN
        ALTER TABLE audit_logs
            ALTER COLUMN ip_address TYPE VARCHAR(255)
            USING host(ip_address);
    ELSE
        ALTER TABLE audit_logs
            ALTER COLUMN ip_address TYPE VARCHAR(255)
            USING ip_address::TEXT;
    END IF;
END;
$$;

-- ============================================================
-- 3. LOGIN_HISTORY.IP_ADDRESS
-- LoginHistory.java also maps ipAddress as String.
-- V14 may already have converted it to VARCHAR(50); normalize to 255.
-- ============================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'login_history'
          AND column_name = 'ip_address'
          AND udt_name = 'inet'
    ) THEN
        ALTER TABLE login_history
            ALTER COLUMN ip_address TYPE VARCHAR(255)
            USING host(ip_address);
    ELSE
        ALTER TABLE login_history
            ALTER COLUMN ip_address TYPE VARCHAR(255)
            USING ip_address::TEXT;
    END IF;
END;
$$;

-- ============================================================
-- 4. FINAL TYPE CHECK
-- Stop migration with a clear error if alignment failed.
-- ============================================================

DO $$
DECLARE
    v_users_email_type TEXT;
    v_audit_ip_type TEXT;
    v_login_ip_type TEXT;
BEGIN
    SELECT data_type
      INTO v_users_email_type
      FROM information_schema.columns
     WHERE table_schema = 'public'
       AND table_name = 'users'
       AND column_name = 'email';

    SELECT data_type
      INTO v_audit_ip_type
      FROM information_schema.columns
     WHERE table_schema = 'public'
       AND table_name = 'audit_logs'
       AND column_name = 'ip_address';

    SELECT data_type
      INTO v_login_ip_type
      FROM information_schema.columns
     WHERE table_schema = 'public'
       AND table_name = 'login_history'
       AND column_name = 'ip_address';

    IF v_users_email_type <> 'character varying' THEN
        RAISE EXCEPTION
            'V15 failed: users.email type is %, expected character varying',
            v_users_email_type;
    END IF;

    IF v_audit_ip_type <> 'character varying' THEN
        RAISE EXCEPTION
            'V15 failed: audit_logs.ip_address type is %, expected character varying',
            v_audit_ip_type;
    END IF;

    IF v_login_ip_type <> 'character varying' THEN
        RAISE EXCEPTION
            'V15 failed: login_history.ip_address type is %, expected character varying',
            v_login_ip_type;
    END IF;
END;
$$;
