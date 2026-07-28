UPDATE users
SET
    profile_image = '/profiles/admin.jpg',
    updated_at = NOW()
WHERE email = 'thavryvateyphal113@gmail.com'
  AND role = 'ADMIN';