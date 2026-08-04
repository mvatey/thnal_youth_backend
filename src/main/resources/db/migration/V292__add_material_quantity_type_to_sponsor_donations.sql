ALTER TABLE donation_sponsor_details
    ALTER COLUMN material_quantity TYPE NUMERIC(15,3)
        USING material_quantity::NUMERIC(15,3);

ALTER TABLE donation_sponsor_details
    ADD COLUMN IF NOT EXISTS material_quantity_type VARCHAR(100);