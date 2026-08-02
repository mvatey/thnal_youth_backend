DO $$
DECLARE
target_branch_id BIGINT := 1;
    creator_user_id BIGINT := 1;

    default_sector_id SMALLINT;

    internal_type_id SMALLINT;
    external_type_id SMALLINT;

    completed_status_id SMALLINT;
    upcoming_status_id SMALLINT;

    tree_image_id BIGINT;
    book_image_id BIGINT;
    dinner_image_id BIGINT;
    meeting_image_id BIGINT;

    inserted_count INTEGER;
BEGIN
    /*
     * Validate required branch and user.
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
     * Resolve one existing sector.
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
     * Register frontend test images.
     */
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

/*
 * Resolve file IDs.
 */
SELECT id
INTO tree_image_id
FROM files
WHERE file_path = '/tree planning.jpg';

SELECT id
INTO book_image_id
FROM files
WHERE file_path = '/book.webp';

SELECT id
INTO dinner_image_id
FROM files
WHERE file_path = '/dinner.webp';

SELECT id
INTO meeting_image_id
FROM files
WHERE file_path = '/meeting.jpg';

/*
 * Insert five recent completed activities and
 * five nearest upcoming activities.
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
    location_name,
    address,
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
    seed.location_name,
    seed.address,
    seed.capacity,
    seed.cover_image_id,
    creator_user_id
FROM (
         VALUES
             /*
              * Recent completed #1
              * Newest completed activity.
              */
             (
                 'កម្មវិធីដាំដើមឈើ',
                 '[DASHBOARD-SEED-01] Tree Planting',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-07-30 08:00:00+07',
                 TIMESTAMPTZ '2026-07-30 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 tree_image_id
             ),

             /*
              * Recent completed #2
              */
             (
                 'កម្មវិធីបរិច្ចាគសៀវភៅ',
                 '[DASHBOARD-SEED-02] Book Donation',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-07-29 08:30:00+07',
                 TIMESTAMPTZ '2026-07-29 11:30:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 book_image_id
             ),

             /*
              * Recent completed #3
              */
             (
                 'កម្មវិធីជប់លៀង',
                 '[DASHBOARD-SEED-03] Dinner Event',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 internal_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-07-28 18:00:00+07',
                 TIMESTAMPTZ '2026-07-28 21:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 dinner_image_id
             ),

             /*
              * Nearest upcoming #1.
              */
             (
                 'កម្មវិធីជួបជុំសមាជិក',
                 '[DASHBOARD-SEED-04] Member Gathering',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-08-01 09:00:00+07',
                 TIMESTAMPTZ '2026-08-01 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 meeting_image_id
             ),

             /*
              * Nearest upcoming #2.
              */
             (
                 'កិច្ចប្រជុំក្រុមប្រឹក្សា',
                 '[DASHBOARD-SEED-05] Council Meeting',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 internal_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-08-02 09:00:00+07',
                 TIMESTAMPTZ '2026-08-02 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 dinner_image_id
             ),

             /*
              * Recent completed #4.
              */
             (
                 'កម្មវិធីដាំដើមឈើ',
                 '[DASHBOARD-SEED-06] Tree Planting',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-07-27 08:00:00+07',
                 TIMESTAMPTZ '2026-07-27 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 tree_image_id
             ),

             /*
              * Recent completed #5.
              */
             (
                 'កម្មវិធីបរិច្ចាគសៀវភៅ',
                 '[DASHBOARD-SEED-07] Book Donation',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 completed_status_id,
                 TIMESTAMPTZ '2026-07-26 08:00:00+07',
                 TIMESTAMPTZ '2026-07-26 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 book_image_id
             ),

             /*
              * Nearest upcoming #3.
              */
             (
                 'កម្មវិធីជប់លៀង',
                 '[DASHBOARD-SEED-08] Dinner Event',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 internal_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-08-03 09:00:00+07',
                 TIMESTAMPTZ '2026-08-03 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 dinner_image_id
             ),

             /*
              * Nearest upcoming #4.
              */
             (
                 'កម្មវិធីជួបជុំសមាជិក',
                 '[DASHBOARD-SEED-09] Member Gathering',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 external_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-08-04 09:00:00+07',
                 TIMESTAMPTZ '2026-08-04 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 meeting_image_id
             ),

             /*
              * Nearest upcoming #5.
              */
             (
                 'កិច្ចប្រជុំក្រុមប្រឹក្សា',
                 '[DASHBOARD-SEED-10] Council Meeting',
                 'កម្មវិធីសាកល្បងសម្រាប់ផ្ទាំងគ្រប់គ្រង',
                 internal_type_id,
                 upcoming_status_id,
                 TIMESTAMPTZ '2026-08-05 09:00:00+07',
                 TIMESTAMPTZ '2026-08-05 11:00:00+07',
                 'រាជធានីភ្នំពេញ',
                 'រាជធានីភ្នំពេញ',
                 200,
                 dinner_image_id
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
                capacity,
                cover_image_id
    )
WHERE NOT EXISTS (
    SELECT 1
    FROM activities existing
    WHERE existing.title_en = seed.title_en
);

GET DIAGNOSTICS inserted_count = ROW_COUNT;

RAISE NOTICE
        'Inserted % dashboard activity records',
        inserted_count;
END $$;