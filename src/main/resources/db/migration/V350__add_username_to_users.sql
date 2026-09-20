ALTER TABLE users ADD COLUMN username VARCHAR(255);

-- Backfill from the account's English name. Duplicates get a numbered
-- suffix so the column can be made UNIQUE below -- first person with a
-- given name keeps it clean, later ones get "Name 2", "Name 3", etc.
WITH ranked AS (
    SELECT
        id,
        full_name_en,
        ROW_NUMBER() OVER (
            PARTITION BY full_name_en
            ORDER BY id
        ) AS rn
    FROM users
    WHERE full_name_en IS NOT NULL
      AND TRIM(full_name_en) <> ''
)
UPDATE users u
SET username = CASE
    WHEN ranked.rn = 1 THEN ranked.full_name_en
    ELSE ranked.full_name_en || ' ' || ranked.rn::text
END
FROM ranked
WHERE ranked.id = u.id;

-- Defensive fallback for any account with no English name on file --
-- phone is already required and unique, so it can never collide here.
UPDATE users
SET username = phone
WHERE username IS NULL
   OR TRIM(username) = '';

ALTER TABLE users
    ALTER COLUMN username SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT uq_users_username UNIQUE (username);
