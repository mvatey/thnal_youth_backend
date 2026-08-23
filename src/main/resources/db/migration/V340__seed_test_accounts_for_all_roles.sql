-- Standalone (member_id IS NULL) test login accounts covering every role
-- and viewer scope, for manual testing. All fifteen share the password
-- "123456" and are created ACTIVE with activated_at already set, so none
-- of them need to go through OTP activation — log in directly.
--
-- password_hash below is bcrypt("123456"), generated with the exact same
-- BCryptPasswordEncoder the app uses (spring-security-crypto), so it
-- verifies correctly through the normal login flow.

INSERT INTO users (
    member_id, branch_id, phone, email, password_hash,
    role, viewer_scope, status, activated_at,
    full_name_km, full_name_en, failed_login_count
)
VALUES
    (NULL, NULL, '099000001', 'tnal.admin@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'ADMIN', NULL, 'ACTIVE', NOW(),
     'Tnal Admin', 'Tnal Admin', 0),

    (NULL, 1, '099000002', 'tnal.secretary1@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'SECRETARY', NULL, 'ACTIVE', NOW(),
     'Tnal Secretary 1', 'Tnal Secretary 1', 0),

    (NULL, 1, '099000003', 'tnal.branchleader1@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'BRANCH_LEADER', NULL, 'ACTIVE', NOW(),
     'Tnal Branch Leader 1', 'Tnal Branch Leader 1', 0),

    (NULL, 2, '099000004', 'tnal.secretary2@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'SECRETARY', NULL, 'ACTIVE', NOW(),
     'Tnal Secretary 2', 'Tnal Secretary 2', 0),

    (NULL, 2, '099000005', 'tnal.branchleader2@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'BRANCH_LEADER', NULL, 'ACTIVE', NOW(),
     'Tnal Branch Leader 2', 'Tnal Branch Leader 2', 0),

    (NULL, 3, '099000006', 'tnal.secretary3@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'SECRETARY', NULL, 'ACTIVE', NOW(),
     'Tnal Secretary 3', 'Tnal Secretary 3', 0),

    (NULL, 1, '099000007', 'tnal.member1@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'MEMBER', NULL, 'ACTIVE', NOW(),
     'Tnal Member 1', 'Tnal Member 1', 0),

    (NULL, 2, '099000008', 'tnal.member2@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'MEMBER', NULL, 'ACTIVE', NOW(),
     'Tnal Member 2', 'Tnal Member 2', 0),

    (NULL, 3, '099000009', 'tnal.member3@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'MEMBER', NULL, 'ACTIVE', NOW(),
     'Tnal Member 3', 'Tnal Member 3', 0),

    (NULL, 1, '099000010', 'tnal.viewerbranchleader1@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'VIEWER', 'BRANCH_LEADER', 'ACTIVE', NOW(),
     'Tnal Viewer Branch Leader 1', 'Tnal Viewer Branch Leader 1', 0),

    (NULL, 1, '099000011', 'tnal.viewersecretary1@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'VIEWER', 'SECRETARY', 'ACTIVE', NOW(),
     'Tnal Viewer Secretary 1', 'Tnal Viewer Secretary 1', 0),

    (NULL, 1, '099000012', 'tnal.viewermember1@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'VIEWER', 'MEMBER', 'ACTIVE', NOW(),
     'Tnal Viewer Member 1', 'Tnal Viewer Member 1', 0),

    (NULL, NULL, '099000013', 'tnal.vieweradmin@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'VIEWER', 'ADMIN', 'ACTIVE', NOW(),
     'Tnal Viewer Admin', 'Tnal Viewer Admin', 0),

    (NULL, 4, '099000014', 'tnal.userbranchleader@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'BRANCH_LEADER', NULL, 'ACTIVE', NOW(),
     'Tnal User Branch Leader', 'Tnal User Branch Leader', 0),

    (NULL, 4, '099000015', 'tnal.usersecretary@test.com',
     '$2a$10$1m2npNQbBLW8zS.Fdgxh4eJOJPSQH9v30PSo9LSxh7EOy1LKKS4AS',
     'SECRETARY', NULL, 'ACTIVE', NOW(),
     'Tnal User Secretary', 'Tnal User Secretary', 0);
