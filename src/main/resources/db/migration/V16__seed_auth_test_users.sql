-- ============================================================
-- V16 — Seed Authentication Test Users
-- Test accounts for all four application roles
-- ============================================================

-- This BCrypt hash must match the password you currently use.
-- Based on your previous authentication seed, the test password
-- should be: 12345

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
SELECT
    '081816685',
    'admin1@gmail.com',
    '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
    'ADMIN',
    'ACTIVE',
    'អ្នកគ្រប់គ្រងប្រព័ន្ធ',
    'System Administrator',
    NULL,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS
          (
              SELECT 1
              FROM users
              WHERE phone = '081816685'
                 OR LOWER(email) = LOWER('admin1@gmail.com')
          );

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
SELECT
    '081816686',
    'leader1@gmail.com',
    '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
    'BRANCH_LEADER',
    'ACTIVE',
    'ប្រធានសាខាសាកល្បង',
    'Test Branch Leader',
    NULL,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS
          (
              SELECT 1
              FROM users
              WHERE phone = '081816686'
                 OR LOWER(email) = LOWER('leader1@gmail.com')
          );

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
SELECT
    '081816687',
    'secretary1@gmail.com',
    '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
    'SECRETARY',
    'ACTIVE',
    'លេខាធិការសាកល្បង',
    'Test Secretary',
    NULL,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS
          (
              SELECT 1
              FROM users
              WHERE phone = '081816687'
                 OR LOWER(email) = LOWER('secretary1@gmail.com')
          );

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
SELECT
    '081816688',
    'member1@gmail.com',
    '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
    'MEMBER',
    'ACTIVE',
    'សមាជិកសាកល្បង',
    'Test Member',
    NULL,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS
          (
              SELECT 1
              FROM users
              WHERE phone = '081816688'
                 OR LOWER(email) = LOWER('member1@gmail.com')
          );