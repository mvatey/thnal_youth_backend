ALTER TABLE member_education
DROP CONSTRAINT IF EXISTS chk_member_education_location;

ALTER TABLE member_education
DROP CONSTRAINT IF EXISTS fk_member_education_province;

DROP INDEX IF EXISTS idx_member_education_province_id;

ALTER TABLE member_education
DROP COLUMN IF EXISTS province_id;

ALTER TABLE member_education
DROP COLUMN IF EXISTS country_code;