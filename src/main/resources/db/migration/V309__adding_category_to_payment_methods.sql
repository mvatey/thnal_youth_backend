-- Add a category for grouping payment methods:
-- CASH, BANK, OTHER

ALTER TABLE payment_methods
    ADD COLUMN category VARCHAR(20);

UPDATE payment_methods
SET category =
        CASE
            WHEN UPPER(code) = 'CASH'
                THEN 'CASH'

            WHEN UPPER(code) IN (
                                 'ABA',
                                 'ACLEDA',
                                 'WING',
                                 'TRUEMONEY'
                )
                THEN 'BANK'

            ELSE 'OTHER'
            END;

ALTER TABLE payment_methods
    ALTER COLUMN category SET NOT NULL;

ALTER TABLE payment_methods
    ADD CONSTRAINT chk_payment_method_category
        CHECK (
            category IN (
                         'CASH',
                         'BANK',
                         'OTHER'
                )
            );