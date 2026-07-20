-- ============================================================
-- V17 — Insert test accounts for all four roles
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
    ),
    (
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
    ),
    (
        '0978974661',
        'thavryvateyphal113@gmail.com',
        '$2a$12$V6UoKo9i5rQl7XKvsth48eUWQNzexITv5RiAgu6VKeNLw5xxJ85Ti',
        'SECRETARY',
        'ACTIVE',
        'លេខាធិការសាកល្បង',
        'Test Secretary',
        NULL,
        0,
        NOW(),
        NOW()
    ),
    (
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
    )

ON CONFLICT DO NOTHING;