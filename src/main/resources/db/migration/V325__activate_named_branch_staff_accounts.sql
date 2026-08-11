-- Activate the named staff accounts used by the existing member/branch data.
-- Keep their existing member links and profile information intact.
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
            'An active branch is required for the named staff accounts';
    END IF;

    UPDATE users
    SET password_hash = '$2a$12$SAADEkTSKP28SUkpwjuAkO8iaFiFimjJ3VOvFUvqYSDslg5a9yf5G',
        role = 'SECRETARY',
        status = 'ACTIVE',
        branch_id = COALESCE(branch_id, v_branch_id),
        activated_at = CURRENT_TIMESTAMP,
        failed_login_count = 0,
        locked_until = NULL,
        updated_at = CURRENT_TIMESTAMP
    WHERE LOWER(email) = LOWER('rithyphan@gmail.com');

    IF NOT FOUND THEN
        RAISE EXCEPTION
            'Secretary account rithyphan@gmail.com was not found';
    END IF;

    UPDATE users
    SET password_hash = '$2a$12$g/Wda6vh5F5uUC4cnK4NGupuSf4IVvIxX0FYTlBQs7ggJG9RzeMvO',
        role = 'BRANCH_LEADER',
        status = 'ACTIVE',
        branch_id = COALESCE(branch_id, v_branch_id),
        activated_at = CURRENT_TIMESTAMP,
        failed_login_count = 0,
        locked_until = NULL,
        updated_at = CURRENT_TIMESTAMP
    WHERE LOWER(email) = LOWER('phatsaproeun@gmail.com');

    IF NOT FOUND THEN
        RAISE EXCEPTION
            'Branch leader account phatsaproeun@gmail.com was not found';
    END IF;
END
$$;
