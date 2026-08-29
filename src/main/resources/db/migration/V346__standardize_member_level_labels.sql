-- Use the same member-level terminology in every language-sensitive list
-- and dropdown. Existing member records retain their level IDs.
UPDATE member_levels
SET
    label_km = CASE code
        WHEN 'LEVEL-ONE' THEN 'កាំ ក'
        WHEN 'LEVEL-TWO' THEN 'កាំ ខ'
        WHEN 'LEVEL-THREE' THEN 'កាំ គ'
        WHEN 'LEVEL-FOUR' THEN 'កាំ ឃ'
        WHEN 'LEVEL-FIVE' THEN 'កាំ ង'
        ELSE label_km
    END,
    label_en = CASE code
        WHEN 'LEVEL-ONE' THEN 'Level 1'
        WHEN 'LEVEL-TWO' THEN 'Level 2'
        WHEN 'LEVEL-THREE' THEN 'Level 3'
        WHEN 'LEVEL-FOUR' THEN 'Level 4'
        WHEN 'LEVEL-FIVE' THEN 'Level 5'
        ELSE label_en
    END,
    updated_at = NOW()
WHERE code IN ('LEVEL-ONE', 'LEVEL-TWO', 'LEVEL-THREE', 'LEVEL-FOUR', 'LEVEL-FIVE');
