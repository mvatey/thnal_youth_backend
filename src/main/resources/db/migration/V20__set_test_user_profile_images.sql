-- ============================================================
-- V18 — Assign profile images to authentication test users
-- ============================================================

UPDATE users
SET
    profile_image = '/profiles/admin.jpg',
    updated_at = NOW()
WHERE phone = '081816685'
   OR LOWER(email) = LOWER('admin1@gmail.com');


UPDATE users
SET
    profile_image = '/profiles/branch-leader.jpg',
    updated_at = NOW()
WHERE phone = '081816686'
   OR LOWER(email) = LOWER('leader1@gmail.com');


UPDATE users
SET
    profile_image = '/profiles/secretary.jpg',
    updated_at = NOW()
WHERE phone = '081816687'
   OR LOWER(email) = LOWER('secretary1@gmail.com');


UPDATE users
SET
    profile_image = '/profiles/member.jpg',
    updated_at = NOW()
WHERE phone = '081816688'
   OR LOWER(email) = LOWER('member1@gmail.com');


UPDATE users
SET
    profile_image = '/profiles/secretary.jpg',
    updated_at = NOW()
WHERE phone = '0978974661'
   OR LOWER(email) =
      LOWER('thavryvateyphal113@gmail.com');