/*z
 * One-time deployment-readiness cleanup.
 *
 * Removes all dev/test seed data inserted by earlier migrations
 * (branches, members, activities, donations, documents, files,
 * and related child tables), while leaving every lookup/reference
 * table untouched (provinces, statuses, types, etc.) and keeping
 * exactly two user accounts: tnal.admin@test.com (ADMIN) and
 * tnal.vieweradmin@test.com (VIEWER).
 *
 * Deletion order follows the FK graph (children before parents)
 * so it works whether or not a given constraint cascades.
 */

DELETE FROM notification_deliveries;
DELETE FROM notification_recipients;
DELETE FROM notifications;

DELETE FROM donation_sponsor_details;
DELETE FROM donations;
DELETE FROM sponsors;

DELETE FROM member_credentials;

DELETE FROM activity_participants;
DELETE FROM activity_invited_branches;
DELETE FROM activity_attachments;
DELETE FROM activity_photos;
DELETE FROM activity_daily_schedules;
DELETE FROM activity_expenses;

DELETE FROM documents;

DELETE FROM member_education;
DELETE FROM member_family;
DELETE FROM member_languages;
DELETE FROM member_political_affiliations;
DELETE FROM member_skills;
DELETE FROM member_work_history;

DELETE FROM login_history;
DELETE FROM refresh_tokens;
DELETE FROM password_reset_tokens;
DELETE FROM telegram_link_tokens;

DELETE FROM members;

DELETE FROM activities;

DELETE FROM branch_staff;

UPDATE branches
SET parent_branch_id = NULL;

DELETE FROM branches;

DELETE FROM files
WHERE id NOT IN (
    SELECT logo_file_id
    FROM organization_profile
    WHERE logo_file_id IS NOT NULL

    UNION

    SELECT cover_file_id
    FROM organization_profile
    WHERE cover_file_id IS NOT NULL
);

DELETE FROM users
WHERE email NOT IN (
    'tnal.admin@test.com',
    'tnal.vieweradmin@test.com'
);

