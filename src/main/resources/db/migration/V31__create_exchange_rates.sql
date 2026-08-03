-- ============================================================
-- V28: Global exchange-rate configuration with history
-- ============================================================

CREATE TABLE exchange_rates (
                                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                from_currency VARCHAR(3) NOT NULL,
                                to_currency VARCHAR(3) NOT NULL,

                                rate NUMERIC(18, 6) NOT NULL,

                                effective_from DATE NOT NULL,
                                effective_to DATE,

                                is_active BOOLEAN NOT NULL DEFAULT TRUE,

                                created_by BIGINT,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                CONSTRAINT fk_exchange_rate_created_by
                                    FOREIGN KEY (created_by)
                                        REFERENCES users(id)
                                        ON DELETE SET NULL,

                                CONSTRAINT chk_exchange_rate_currencies
                                    CHECK (from_currency <> to_currency),

                                CONSTRAINT chk_exchange_rate_positive
                                    CHECK (rate > 0),

                                CONSTRAINT chk_exchange_rate_dates
                                    CHECK (
                                        effective_to IS NULL
                                            OR effective_to >= effective_from
                                        )
);

CREATE INDEX idx_exchange_rates_currency_pair
    ON exchange_rates(from_currency, to_currency);

CREATE INDEX idx_exchange_rates_effective_dates
    ON exchange_rates(
                      from_currency,
                      to_currency,
                      effective_from,
                      effective_to
        );

CREATE INDEX idx_exchange_rates_active
    ON exchange_rates(
                      from_currency,
                      to_currency,
                      is_active
        );

-- Only one active rate for each currency pair.
CREATE UNIQUE INDEX uq_exchange_rates_active_pair
    ON exchange_rates(from_currency, to_currency)
    WHERE is_active = TRUE;

-- Initial system rate:
-- 1 USD = 4000 KHR
INSERT INTO exchange_rates (
    from_currency,
    to_currency,
    rate,
    effective_from,
    effective_to,
    is_active,
    created_by
)
VALUES (
           'USD',
           'KHR',
           4000.000000,
           CURRENT_DATE,
           NULL,
           TRUE,
           NULL
       );