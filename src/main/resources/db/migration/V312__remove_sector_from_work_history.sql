-- Vxxx__remove_employment_sector_from_work_history.sql

ALTER TABLE member_work_history
DROP CONSTRAINT IF EXISTS fk_member_work_history_sector;

DROP INDEX IF EXISTS idx_member_work_history_sector_id;

ALTER TABLE member_work_history
DROP COLUMN IF EXISTS employment_sector_id;