DO $$
DECLARE
target_branch_id BIGINT := 1;
    creator_user_id BIGINT := 1;

    default_sector_id SMALLINT;

    internal_type_id SMALLINT;
    external_type_id SMALLINT;

    completed_status_id SMALLINT;
    upcoming_status_id SMALLINT;
BEGIN
    /*
     * Verify the branch.
     */
    IF NOT EXISTS (
        SELECT 1
        FROM branches
        WHERE id = target_branch_id
    ) THEN
        RAISE EXCEPTION
            'Branch with ID % does not exist',
            target_branch_id;
END IF;

    /*
     * Verify the creator user.
     */
    IF NOT EXISTS (
        SELECT 1
        FROM users
        WHERE id = creator_user_id
    ) THEN
        RAISE EXCEPTION
            'User with ID % does not exist',
            creator_user_id;
END IF;

    /*
     * Select one existing activity sector.
     */
SELECT id
INTO default_sector_id
FROM activity_sectors
ORDER BY id
    LIMIT 1;

IF default_sector_id IS NULL THEN
        RAISE EXCEPTION
            'No activity sector exists';
END IF;

    /*
     * Resolve activity types.
     */
SELECT id
INTO internal_type_id
FROM activity_types
WHERE UPPER(code) = 'INTERNAL'
    LIMIT 1;

SELECT id
INTO external_type_id
FROM activity_types
WHERE UPPER(code) = 'EXTERNAL'
    LIMIT 1;

IF internal_type_id IS NULL THEN
        RAISE EXCEPTION
            'Activity type INTERNAL does not exist';
END IF;

    IF external_type_id IS NULL THEN
        RAISE EXCEPTION
            'Activity type EXTERNAL does not exist';
END IF;

    /*
     * Resolve activity statuses.
     */
SELECT id
INTO completed_status_id
FROM activity_statuses
WHERE UPPER(code) = 'COMPLETED'
    LIMIT 1;

SELECT id
INTO upcoming_status_id
FROM activity_statuses
WHERE UPPER(code) = 'UPCOMING'
    LIMIT 1;

IF completed_status_id IS NULL THEN
        RAISE EXCEPTION
            'Activity status COMPLETED does not exist';
END IF;

    IF upcoming_status_id IS NULL THEN
        RAISE EXCEPTION
            'Activity status UPCOMING does not exist';
END IF;

    /*
     * Insert dashboard activity test records.
     *
     * title_en values contain unique seed codes so the same
     * query does not insert duplicates when it runs again.
     */
INSERT INTO activities (
    title_km,
    title_en,
    description,
    type_id,
    sector_id,
    status_id,
    branch_id,
    is_public,
    starts_at,
    ends_at,
    province_id,
    district_id,
    commune_id,
    location_name,
    address,
    google_map_url,
    capacity,
    cover_image_id,
    created_by
)
SELECT
    seed.title_km,
    seed.title_en,
    seed.description,
    seed.type_id,
    default_sector_id,
    seed.status_id,
    target_branch_id,
    TRUE,
    seed.starts_at,
    seed.ends_at,
    NULL,
    NULL,
    NULL,
    seed.location_name,
    seed.address,
    NULL,
    seed.capacity,
    NULL,
    creator_user_id
FROM (
         VALUES
             /*
              * 1
              */
             (
                 'កម្មវិធីដាំដើមឈើ',
                 '[DASHBOARD-SEED-01] Tree Planting',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-01-25 08:00:00+07',
                 TIMESTAMPTZ '2026-01-25 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 2
              */
             (
                 'កម្មវិធីបរិច្ចាគសៀវភៅ',
                 '[DASHBOARD-SEED-02] Book Donation',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-01-25 08:00:00+07',
                 TIMESTAMPTZ '2026-01-25 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 3
              */
             (
                 'កម្មវិធីជប់លៀង',
                 '[DASHBOARD-SEED-03] Dinner Event',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 internal_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-01-25 18:00:00+07',
                 TIMESTAMPTZ '2026-01-25 21:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 4
              */
             (
                 'កម្មវិធីជួបជុំសមាជិក',
                 '[DASHBOARD-SEED-04] Member Gathering',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-02-06 09:00:00+07',
                 TIMESTAMPTZ '2026-02-06 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 5
              */
             (
                 'កិច្ចប្រជុំក្រុមប្រឹក្សា',
                 '[DASHBOARD-SEED-05] Council Meeting',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 internal_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-02-06 09:00:00+07',
                 TIMESTAMPTZ '2026-02-06 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 6
              */
             (
                 'កម្មវិធីដាំដើមឈើ',
                 '[DASHBOARD-SEED-06] Tree Planting',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-01-25 08:00:00+07',
                 TIMESTAMPTZ '2026-01-25 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 7
              */
             (
                 'កម្មវិធីបរិច្ចាគសៀវភៅ',
                 '[DASHBOARD-SEED-07] Book Donation',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-01-25 08:00:00+07',
                 TIMESTAMPTZ '2026-01-25 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 8
              */
             (
                 'កម្មវិធីជប់លៀង',
                 '[DASHBOARD-SEED-08] Dinner Event',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 internal_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-01-25 09:00:00+07',
                 TIMESTAMPTZ '2026-01-25 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 9
              */
             (
                 'កម្មវិធីជួបជុំសមាជិក',
                 '[DASHBOARD-SEED-09] Member Gathering',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-02-06 09:00:00+07',
                 TIMESTAMPTZ '2026-02-06 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             ),

             /*
              * 10
              */
             (
                 'កិច្ចប្រជុំក្រុមប្រឹក្សា',
                 '[DASHBOARD-SEED-10] Council Meeting',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 internal_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-02-06 09:00:00+07',
                 TIMESTAMPTZ '2026-02-06 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200
             )
     ) AS seed (
                title_km,
                title_en,
                description,
                type_id,
                status_id,
                starts_at,
                ends_at,
                location_name,
                address,
                capacity
    )
WHERE NOT EXISTS (
    SELECT 1
    FROM activities existing
    WHERE existing.title_en = seed.title_en
);

RAISE NOTICE
        'Dashboard activities inserted successfully';
END $$;

DELETE FROM activities
WHERE title_en LIKE '[DASHBOARD-SEED-%';

DO $$
DECLARE
creator_user_id BIGINT := 1;
BEGIN

INSERT INTO files (
    file_path,
    original_name,
    mime_type,
    size_bytes,
    uploaded_by
)
VALUES

    (
        '/tree planning.jpg',
        'tree planning.jpg',
        'image/jpeg',
        120000,
        creator_user_id
    ),

    (
        '/book.webp',
        'book.webp',
        'image/webp',
        95000,
        creator_user_id
    ),

    (
        '/dinner.webp',
        'dinner.webp',
        'image/webp',
        110000,
        creator_user_id
    ),

    (
        '/meeting.jpg',
        'meeting.jpg',
        'image/jpeg',
        135000,
        creator_user_id
    )

    ON CONFLICT (file_path)
    DO NOTHING;

END $$;

UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-01]%'
  AND f.file_path='/tree planning.jpg';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-02]%'
  AND f.file_path='/book.webp';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-03]%'
  AND f.file_path='/dinner.webp';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-04]%'
  AND f.file_path='/meeting.jpg';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-05]%'
  AND f.file_path='/dinner.webp';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-06]%'
  AND f.file_path='/tree planning.jpg';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-07]%'
  AND f.file_path='/book.webp';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-08]%'
  AND f.file_path='/dinner.webp';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-09]%'
  AND f.file_path='/meeting.jpg';


UPDATE activities a
SET cover_image_id = f.id
    FROM files f
WHERE
    a.title_en LIKE '[DASHBOARD-SEED-10]%'
  AND f.file_path='/dinner.webp';