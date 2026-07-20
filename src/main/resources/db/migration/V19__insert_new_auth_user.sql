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
    WHERE NOT EXISTS
          (
              SELECT 1
              FROM users
              WHERE phone = '0978974661'
                 OR LOWER(email) = LOWER('thavryvateyphal113@gmail.com')
          );