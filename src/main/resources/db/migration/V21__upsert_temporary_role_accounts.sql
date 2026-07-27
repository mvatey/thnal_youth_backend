-- ============================================================
-- V21 — Upsert temporary authentication accounts
-- Four test users for ADMIN, BRANCH_LEADER, SECRETARY, MEMBER
--
-- Temporary password for all accounts: 12345
-- BCrypt hash:
-- $2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti
-- ============================================================


-- ============================================================
-- ADMIN
-- ============================================================

INSERT INTO users
(
    phone,
    email,
    password_hash,
    role,
    status,
    full_name_km,
    full_name_en,
    profile_image,
    failed_login_count,
    created_at,
    updated_at
)
VALUES
    (
        '0978974661',
        'thavryvateyphal113@gmail.com',
        '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
        'ADMIN',
        'ACTIVE',
        'ថាវរីវតី',
        'Thavry Vatey',
        '/profiles/admin.jpg',
        0,
        NOW(),
        NOW()
    )
    ON CONFLICT (phone)
DO UPDATE SET
    email = EXCLUDED.email,
           password_hash = EXCLUDED.password_hash,
           role = EXCLUDED.role,
           status = EXCLUDED.status,
           full_name_km = EXCLUDED.full_name_km,
           full_name_en = EXCLUDED.full_name_en,
           profile_image = EXCLUDED.profile_image,
           failed_login_count = 0,
           locked_until = NULL,
           updated_at = NOW();


-- ============================================================
-- BRANCH LEADER
-- ============================================================

INSERT INTO users
(
    phone,
    email,
    password_hash,
    role,
    status,
    full_name_km,
    full_name_en,
    profile_image,
    failed_login_count,
    created_at,
    updated_at
)
VALUES
    (
        '012121212',
        'phatsaproeun@gmail.com',
        '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
        'BRANCH_LEADER',
        'ACTIVE',
        'ព្រឿន ផាត់សា',
        'Proeun Phatsa',
        '/profiles/branch_leader.jpg',
        0,
        NOW(),
        NOW()
    )
    ON CONFLICT (phone)
DO UPDATE SET
    email = EXCLUDED.email,
           password_hash = EXCLUDED.password_hash,
           role = EXCLUDED.role,
           status = EXCLUDED.status,
           full_name_km = EXCLUDED.full_name_km,
           full_name_en = EXCLUDED.full_name_en,
           profile_image = EXCLUDED.profile_image,
           failed_login_count = 0,
           locked_until = NULL,
           updated_at = NOW();


-- ============================================================
-- SECRETARY
-- ============================================================

INSERT INTO users
(
    phone,
    email,
    password_hash,
    role,
    status,
    full_name_km,
    full_name_en,
    profile_image,
    failed_login_count,
    created_at,
    updated_at
)
VALUES
    (
        '081816685',
        'rithyphan@gmail.com',
        '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
        'SECRETARY',
        'ACTIVE',
        'ផាន រិទ្ធី',
        'Phan Rithy',
        '/profiles/secretary.jpg',
        0,
        NOW(),
        NOW()
    )
    ON CONFLICT (phone)
DO UPDATE SET
    email = EXCLUDED.email,
           password_hash = EXCLUDED.password_hash,
           role = EXCLUDED.role,
           status = EXCLUDED.status,
           full_name_km = EXCLUDED.full_name_km,
           full_name_en = EXCLUDED.full_name_en,
           profile_image = EXCLUDED.profile_image,
           failed_login_count = 0,
           locked_until = NULL,
           updated_at = NOW();


-- ============================================================
-- MEMBER
-- ============================================================

INSERT INTO users
(
    phone,
    email,
    password_hash,
    role,
    status,
    full_name_km,
    full_name_en,
    profile_image,
    failed_login_count,
    created_at,
    updated_at
)
VALUES
    (
        '012345678',
        'riyady@gmail.com',
        '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
        'MEMBER',
        'ACTIVE',
        'ឌី រីយ៉ា',
        'Dy Riya',
        '/profiles/member.jpg',
        0,
        NOW(),
        NOW()
    )
    ON CONFLICT (phone)
DO UPDATE SET
    email = EXCLUDED.email,
           password_hash = EXCLUDED.password_hash,
           role = EXCLUDED.role,
           status = EXCLUDED.status,
           full_name_km = EXCLUDED.full_name_km,
           full_name_en = EXCLUDED.full_name_en,
           profile_image = EXCLUDED.profile_image,
           failed_login_count = 0,
           locked_until = NULL,
           updated_at = NOW();