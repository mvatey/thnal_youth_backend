CREATE TABLE IF NOT EXISTS donation_sponsor_details (
                                                        donation_id BIGINT PRIMARY KEY,

                                                        donor_kind VARCHAR(20) NOT NULL,

                                                        material_category VARCHAR(100),

                                                        material_quantity INTEGER,

                                                        purpose VARCHAR(255),

                                                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                                        CONSTRAINT fk_donation_sponsor_details_donation
                                                            FOREIGN KEY (donation_id)
                                                                REFERENCES donations(id)
                                                                ON DELETE CASCADE,

                                                        CONSTRAINT chk_donation_sponsor_details_donor_kind
                                                            CHECK (
                                                                donor_kind IN (
                                                                               'INDIVIDUAL',
                                                                               'INSTITUTION',
                                                                               'MEMBER'
                                                                    )
                                                                ),

                                                        CONSTRAINT chk_donation_sponsor_details_material_quantity
                                                            CHECK (
                                                                material_quantity IS NULL
                                                                    OR material_quantity > 0
                                                                )
);

CREATE INDEX IF NOT EXISTS idx_donation_sponsor_details_donor_kind
    ON donation_sponsor_details (donor_kind);

COMMENT ON TABLE donation_sponsor_details IS
    'Additional Sponsor Donation fields required by the frontend UI';

COMMENT ON COLUMN donation_sponsor_details.donor_kind IS
    'INDIVIDUAL, INSTITUTION, or MEMBER';

COMMENT ON COLUMN donation_sponsor_details.material_category IS
    'Type/category of donated material when payment method is MATERIAL';

COMMENT ON COLUMN donation_sponsor_details.material_quantity IS
    'Quantity of donated materials';

COMMENT ON COLUMN donation_sponsor_details.purpose IS
    'Purpose or destination of the sponsor donation';