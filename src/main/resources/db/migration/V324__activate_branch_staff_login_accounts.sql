-- Restore two non-production staff accounts after V323 disabled the old
-- compromised demo password. These accounts are intentionally ready for
-- direct role-based UI testing.
DO $$
DECLARE
    v_branch_id BIGINT;
BEGIN
    SELECT b.id
    INTO v_branch_id
    FROM branches b
    JOIN branch_statuses bs ON bs.id = b.status_id
    WHERE bs.code = 'ACTIVE'
    ORDER BY b.id
    LIMIT 1;

    IF v_branch_id IS NULL THEN
        RAISE EXCEPTION
            'An active branch is required for the staff login accounts';
    END IF;

    UPDATE users
    SET phone = '081816686',
        password_hash = '$2a$12$g/Wda6vh5F5uUC4cnK4NGupuSf4IVvIxX0FYTlBQs7ggJG9RzeMvO',
        role = 'BRANCH_LEADER',
        status = 'ACTIVE',
        full_name_km = 'ប្រធានសាខាសាកល្បង',
        full_name_en = 'Test Branch Leader',
        profile_image = COALESCE(profile_image, '/profiles/default-avatar.png'),
        branch_id = v_branch_id,
        activated_at = CURRENT_TIMESTAMP,
        failed_login_count = 0,
        locked_until = NULL,
        updated_at = CURRENT_TIMESTAMP
    WHERE LOWER(email) = LOWER('leader1@gmail.com');

    IF NOT FOUND THEN
        INSERT INTO users (
        phone,
        email,
        password_hash,
        role,
        status,
        full_name_km,
        full_name_en,
        profile_image,
        branch_id,
        activated_at,
        failed_login_count,
        locked_until,
        created_at,
        updated_at
    )
    VALUES (
        '081816686',
        'leader1@gmail.com',
        '$2a$12$g/Wda6vh5F5uUC4cnK4NGupuSf4IVvIxX0FYTlBQs7ggJG9RzeMvO',
        'BRANCH_LEADER',
        'ACTIVE',
        'ប្រធានសាខាសាកល្បង',
        'Test Branch Leader',
        '/profiles/default-avatar.png',
        v_branch_id,
        CURRENT_TIMESTAMP,
        0,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
        );
    END IF;

    UPDATE users
    SET phone = '081816687',
        password_hash = '$2a$12$SAADEkTSKP28SUkpwjuAkO8iaFiFimjJ3VOvFUvqYSDslg5a9yf5G',
        role = 'SECRETARY',
        status = 'ACTIVE',
        full_name_km = 'លេខាធិការសាកល្បង',
        full_name_en = 'Test Secretary',
        profile_image = COALESCE(profile_image, '/profiles/default-avatar.png'),
        branch_id = v_branch_id,
        activated_at = CURRENT_TIMESTAMP,
        failed_login_count = 0,
        locked_until = NULL,
        updated_at = CURRENT_TIMESTAMP
    WHERE LOWER(email) = LOWER('secretary1@gmail.com');

    IF NOT FOUND THEN
        INSERT INTO users (
        phone,
        email,
        password_hash,
        role,
        status,
        full_name_km,
        full_name_en,
        profile_image,
        branch_id,
        activated_at,
        failed_login_count,
        locked_until,
        created_at,
        updated_at
    )
    VALUES (
        '081816687',
        'secretary1@gmail.com',
        '$2a$12$SAADEkTSKP28SUkpwjuAkO8iaFiFimjJ3VOvFUvqYSDslg5a9yf5G',
        'SECRETARY',
        'ACTIVE',
        'លេខាធិការសាកល្បង',
        'Test Secretary',
        '/profiles/default-avatar.png',
        v_branch_id,
        CURRENT_TIMESTAMP,
        0,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
        );
    END IF;
END
$$;
