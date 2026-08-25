ALTER TABLE organization_profile
    ADD COLUMN IF NOT EXISTS hero_headline_km VARCHAR(255),
    ADD COLUMN IF NOT EXISTS hero_headline_en VARCHAR(255),
    ADD COLUMN IF NOT EXISTS hero_description_km TEXT,
    ADD COLUMN IF NOT EXISTS hero_description_en TEXT;
