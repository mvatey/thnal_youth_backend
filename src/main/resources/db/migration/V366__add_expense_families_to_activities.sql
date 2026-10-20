-- A gift-distribution activity ("ចុះចែកអំណោយ") records its expenses per
-- family: several families can receive gifts in one activity, and each family
-- receives several items (the existing expense rows). A family is identified
-- by the name of one of its members and the reason it was chosen.

CREATE TABLE IF NOT EXISTS activity_expense_families (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    activity_id BIGINT       NOT NULL
        REFERENCES activities (id) ON DELETE CASCADE,
    member_name VARCHAR(255) NOT NULL,
    reason      TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_activity_expense_family_name
        CHECK (BTRIM(member_name) <> '')
);

CREATE INDEX IF NOT EXISTS idx_activity_expense_families_activity
    ON activity_expense_families (activity_id);

-- Which family an expense row belongs to (null for every other activity type).
ALTER TABLE activity_expenses
    ADD COLUMN IF NOT EXISTS family_id BIGINT
        REFERENCES activity_expense_families (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_activity_expenses_family
    ON activity_expenses (family_id);
