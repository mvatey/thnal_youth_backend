--44. refresh_tokens
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE refresh_tokens (
                                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                user_id BIGINT NOT NULL,

                                token UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),

                                expires_at TIMESTAMPTZ NOT NULL,
                                revoked_at TIMESTAMPTZ,

                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                CONSTRAINT fk_refresh_token_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT chk_refresh_token_expiry
                                    CHECK (expires_at > created_at),

                                CONSTRAINT chk_refresh_token_revocation
                                    CHECK (
                                        revoked_at IS NULL
                                            OR (
                                            revoked_at >= created_at
                                                AND revoked_at <= expires_at
                                            )
                                        )
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);

CREATE INDEX idx_refresh_tokens_active
    ON refresh_tokens(token, expires_at)
    WHERE revoked_at IS NULL;


--45. password_reset_tokens
CREATE TABLE password_reset_tokens (
                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                       user_id BIGINT NOT NULL,

                                       otp_code_hash TEXT NOT NULL,

                                       delivery_channel VARCHAR(20) NOT NULL,

                                       expires_at TIMESTAMPTZ NOT NULL,
                                       consumed_at TIMESTAMPTZ,

                                       attempts INT NOT NULL DEFAULT 0,

                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                       CONSTRAINT fk_password_reset_user
                                           FOREIGN KEY (user_id)
                                               REFERENCES users(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT chk_password_reset_otp_hash
                                           CHECK (BTRIM(otp_code_hash) <> ''),

                                       CONSTRAINT chk_password_reset_channel
                                           CHECK (
                                               delivery_channel IN ('SMS', 'EMAIL')
                                               ),

                                       CONSTRAINT chk_password_reset_attempts
                                           CHECK (
                                               attempts >= 0
                                                   AND attempts <= 10
                                               ),

                                       CONSTRAINT chk_password_reset_expiry
                                           CHECK (expires_at > created_at),

                                       CONSTRAINT chk_password_reset_consumed_at
                                           CHECK (
                                               consumed_at IS NULL
                                                   OR (
                                                   consumed_at >= created_at
                                                       AND consumed_at <= expires_at
                                                   )
                                               )
);

CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens(user_id);

CREATE INDEX idx_password_reset_tokens_expires_at
    ON password_reset_tokens(expires_at);

CREATE INDEX idx_password_reset_tokens_active
    ON password_reset_tokens(user_id, expires_at)
    WHERE consumed_at IS NULL;


--46. login_history
CREATE TABLE login_history (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               user_id BIGINT,

                               login_identifier VARCHAR(255),

                               success BOOLEAN NOT NULL,

                               failure_reason VARCHAR(100),

                               ip_address INET,
                               user_agent TEXT,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_login_history_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE SET NULL,

                               CONSTRAINT chk_login_history_identifier
                                   CHECK (
                                       login_identifier IS NULL
                                           OR BTRIM(login_identifier) <> ''
                                       ),

                               CONSTRAINT chk_login_history_failure_reason
                                   CHECK (
                                       (
                                           success = TRUE
                                               AND failure_reason IS NULL
                                           )
                                           OR
                                       (
                                           success = FALSE
                                               AND failure_reason IS NOT NULL
                                               AND BTRIM(failure_reason) <> ''
                                           )
                                       )
);

CREATE INDEX idx_login_history_user_id
    ON login_history(user_id);

CREATE INDEX idx_login_history_created_at
    ON login_history(created_at DESC);

CREATE INDEX idx_login_history_success_created_at
    ON login_history(success, created_at DESC);

CREATE INDEX idx_login_history_ip_address
    ON login_history(ip_address);


--47. audit_logs
CREATE TABLE audit_logs (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                            user_id BIGINT,

                            action VARCHAR(50) NOT NULL,

                            entity VARCHAR(100) NOT NULL,
                            entity_id BIGINT,

                            before_data JSONB,
                            after_data JSONB,

                            ip_address INET,
                            user_agent TEXT,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                            CONSTRAINT fk_audit_log_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE SET NULL,

                            CONSTRAINT chk_audit_log_action
                                CHECK (BTRIM(action) <> ''),

                            CONSTRAINT chk_audit_log_entity
                                CHECK (BTRIM(entity) <> '')
);

CREATE INDEX idx_audit_logs_user_id
    ON audit_logs(user_id);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs(entity, entity_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs(action);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs(created_at DESC);

CREATE INDEX idx_audit_logs_user_created_at
    ON audit_logs(user_id, created_at DESC);
