-- ============================================================
-- V29: Align activity expenses with the current Expense UI
-- ============================================================
-- Quantity is descriptive only.
-- amount_khr and amount_usd are the actual amounts spent.
-- The backend converts KHR to USD and calculates total_amount_usd.
-- Historical exchange-rate information is stored internally.

-- Remove constraints related to the old unit-price model.
ALTER TABLE activity_expenses
    DROP CONSTRAINT IF EXISTS chk_activity_expense_unit_price,
    DROP CONSTRAINT IF EXISTS chk_activity_expense_currency,
    DROP CONSTRAINT IF EXISTS chk_activity_expense_total;

-- Add the fields required by the current UI and history flow.
ALTER TABLE activity_expenses
    ADD COLUMN amount_khr NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN amount_usd NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN exchange_rate_id BIGINT,
    ADD COLUMN exchange_rate_value NUMERIC(18, 6),
    ADD COLUMN converted_khr_to_usd NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN total_amount_usd NUMERIC(14, 2) NOT NULL DEFAULT 0;

-- Link the expense to the exact historical exchange-rate record used.
ALTER TABLE activity_expenses
    ADD CONSTRAINT fk_activity_expense_exchange_rate
        FOREIGN KEY (exchange_rate_id)
            REFERENCES exchange_rates(id)
            ON DELETE RESTRICT;

-- Validate monetary values.
ALTER TABLE activity_expenses
    ADD CONSTRAINT chk_activity_expense_amount_khr
        CHECK (amount_khr >= 0),

    ADD CONSTRAINT chk_activity_expense_amount_usd
        CHECK (amount_usd >= 0),

    ADD CONSTRAINT chk_activity_expense_has_amount
        CHECK (
            amount_khr > 0
                OR amount_usd > 0
            ),

    ADD CONSTRAINT chk_activity_expense_exchange_rate_value
        CHECK (
            exchange_rate_value IS NULL
                OR exchange_rate_value > 0
            ),

    ADD CONSTRAINT chk_activity_expense_converted_khr_to_usd
        CHECK (converted_khr_to_usd >= 0),

    ADD CONSTRAINT chk_activity_expense_total_amount_usd
        CHECK (total_amount_usd >= 0);

-- Index for history and financial reporting.
CREATE INDEX idx_activity_expenses_exchange_rate_id
    ON activity_expenses(exchange_rate_id);

-- Remove fields from the old unit-price/currency model.
ALTER TABLE activity_expenses
    DROP COLUMN unit_price,
    DROP COLUMN currency,
    DROP COLUMN total_amount;