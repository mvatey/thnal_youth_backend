--42. donations
CREATE TABLE donations (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           donation_no VARCHAR(50) NOT NULL UNIQUE,

                           donation_type_id SMALLINT NOT NULL,

                           member_id BIGINT,
                           sponsor_id BIGINT,
                           donor_name VARCHAR(255),

                           activity_id BIGINT,

                           branch_id BIGINT NOT NULL,

                           donation_period DATE,

                           amount_khr NUMERIC(14,2) NOT NULL DEFAULT 0,
                           amount_usd NUMERIC(14,2) NOT NULL DEFAULT 0,

                           exchange_rate_khr_per_usd NUMERIC(14,4),
                           total_amount_usd NUMERIC(14,2) NOT NULL DEFAULT 0,

                           payment_method_id SMALLINT NOT NULL,

                           paid_at TIMESTAMPTZ NOT NULL,

                           payment_reference VARCHAR(100),
                           receipt_file_id BIGINT,

                           recorded_by BIGINT NOT NULL,

                           note TEXT,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_donation_type
                               FOREIGN KEY (donation_type_id)
                                   REFERENCES donation_types(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_donation_member
                               FOREIGN KEY (member_id)
                                   REFERENCES members(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_donation_sponsor
                               FOREIGN KEY (sponsor_id)
                                   REFERENCES sponsors(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_donation_activity
                               FOREIGN KEY (activity_id)
                                   REFERENCES activities(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_donation_branch
                               FOREIGN KEY (branch_id)
                                   REFERENCES branches(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_donation_payment_method
                               FOREIGN KEY (payment_method_id)
                                   REFERENCES payment_methods(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_donation_receipt
                               FOREIGN KEY (receipt_file_id)
                                   REFERENCES files(id)
                                   ON DELETE SET NULL,

                           CONSTRAINT fk_donation_recorded_by
                               FOREIGN KEY (recorded_by)
                                   REFERENCES users(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT chk_donation_number
                               CHECK (BTRIM(donation_no) <> ''),

                           CONSTRAINT chk_donation_amounts
                               CHECK (
                                   amount_khr >= 0
                                       AND amount_usd >= 0
                                       AND (
                                       amount_khr > 0
                                           OR amount_usd > 0
                                       )
                                   ),

                           CONSTRAINT chk_donation_exchange_rate
                               CHECK (
                                   exchange_rate_khr_per_usd IS NULL
                                       OR exchange_rate_khr_per_usd > 0
                                   ),

                           CONSTRAINT chk_donation_total
                               CHECK (total_amount_usd >= 0),

                           CONSTRAINT chk_donation_donor_name
                               CHECK (
                                   donor_name IS NULL
                                       OR BTRIM(donor_name) <> ''
                                   ),

                           CONSTRAINT chk_donation_source
                               CHECK (
                                   (
                                       CASE
                                           WHEN member_id IS NOT NULL THEN 1
                                           ELSE 0
                                           END
                                           +
                                       CASE
                                           WHEN sponsor_id IS NOT NULL THEN 1
                                           ELSE 0
                                           END
                                           +
                                       CASE
                                           WHEN NULLIF(BTRIM(donor_name), '') IS NOT NULL THEN 1
                                           ELSE 0
                                           END
                                       ) = 1
                                   )
);

CREATE INDEX idx_donations_type_id
    ON donations(donation_type_id);

CREATE INDEX idx_donations_member_id
    ON donations(member_id);

CREATE INDEX idx_donations_sponsor_id
    ON donations(sponsor_id);

CREATE INDEX idx_donations_activity_id
    ON donations(activity_id);

CREATE INDEX idx_donations_branch_id
    ON donations(branch_id);

CREATE INDEX idx_donations_payment_method_id
    ON donations(payment_method_id);

CREATE INDEX idx_donations_paid_at
    ON donations(paid_at DESC);

CREATE INDEX idx_donations_period
    ON donations(donation_period)
    WHERE donation_period IS NOT NULL;