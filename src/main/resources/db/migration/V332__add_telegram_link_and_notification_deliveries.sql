-- Adds Telegram account-linking support and per-channel notification
-- delivery tracking for the email + Telegram notification feature.
--
-- IMPORTANT: this filename assumes V331 is the latest applied migration
-- (that was the newest one visible when this was drafted). Rename this
-- file's "V332" prefix to whatever your actual next sequential Flyway
-- version is before running it.

-- ---------------------------------------------------------------------
-- 1. Telegram linkage on users
-- ---------------------------------------------------------------------

ALTER TABLE users
    ADD COLUMN telegram_chat_id BIGINT,
    ADD COLUMN telegram_linked_at TIMESTAMPTZ;

ALTER TABLE users
    ADD CONSTRAINT uq_users_telegram_chat_id UNIQUE (telegram_chat_id);

-- ---------------------------------------------------------------------
-- 2. One-time-ish "connect your Telegram" link tokens
-- ---------------------------------------------------------------------

CREATE TABLE telegram_link_tokens (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token        VARCHAR(64) NOT NULL,
    expires_at   TIMESTAMPTZ NOT NULL,
    consumed_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_telegram_link_tokens_token UNIQUE (token)
);

CREATE INDEX idx_telegram_link_tokens_user_active
    ON telegram_link_tokens (user_id, expires_at)
    WHERE consumed_at IS NULL;

-- ---------------------------------------------------------------------
-- 3. Per-channel delivery tracking (observability only — the actual
--    in-app notification/recipient rows remain the source of truth)
-- ---------------------------------------------------------------------

CREATE TABLE notification_deliveries (
    id               BIGSERIAL PRIMARY KEY,
    notification_id  BIGINT NOT NULL REFERENCES notifications (id) ON DELETE CASCADE,
    user_id          BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    channel          VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    error_message    TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_notification_deliveries_channel
        CHECK (channel IN ('EMAIL', 'TELEGRAM')),

    CONSTRAINT chk_notification_deliveries_status
        CHECK (status IN ('SENT', 'FAILED', 'SKIPPED'))
);

CREATE INDEX idx_notification_deliveries_notification
    ON notification_deliveries (notification_id);

CREATE INDEX idx_notification_deliveries_user
    ON notification_deliveries (user_id);
