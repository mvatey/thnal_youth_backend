--48. notifications
CREATE TABLE notifications (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                               type_id SMALLINT NOT NULL,

                               title TEXT NOT NULL,
                               body TEXT NOT NULL,

                               action_url TEXT,

                               activity_id BIGINT,
                               branch_id BIGINT,

                               created_by BIGINT,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_notification_type
                                   FOREIGN KEY (type_id)
                                       REFERENCES notification_types(id)
                                       ON DELETE RESTRICT,

                               CONSTRAINT fk_notification_activity
                                   FOREIGN KEY (activity_id)
                                       REFERENCES activities(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_notification_branch
                                   FOREIGN KEY (branch_id)
                                       REFERENCES branches(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_notification_creator
                                   FOREIGN KEY (created_by)
                                       REFERENCES users(id)
                                       ON DELETE SET NULL,

                               CONSTRAINT chk_notification_title
                                   CHECK (BTRIM(title) <> ''),

                               CONSTRAINT chk_notification_body
                                   CHECK (BTRIM(body) <> ''),

                               CONSTRAINT chk_notification_action_url
                                   CHECK (
                                       action_url IS NULL
                                           OR BTRIM(action_url) <> ''
                                       )
);

CREATE INDEX idx_notifications_type_id
    ON notifications(type_id);

CREATE INDEX idx_notifications_activity_id
    ON notifications(activity_id);

CREATE INDEX idx_notifications_branch_id
    ON notifications(branch_id);

CREATE INDEX idx_notifications_created_by
    ON notifications(created_by);

CREATE INDEX idx_notifications_created_at
    ON notifications(created_at DESC);


--49. notification_recipients
CREATE TABLE notification_recipients (
                                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                         notification_id BIGINT NOT NULL,
                                         user_id BIGINT NOT NULL,

                                         is_read BOOLEAN NOT NULL DEFAULT FALSE,
                                         read_at TIMESTAMPTZ,

                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                         CONSTRAINT fk_notification_recipient_notification
                                             FOREIGN KEY (notification_id)
                                                 REFERENCES notifications(id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT fk_notification_recipient_user
                                             FOREIGN KEY (user_id)
                                                 REFERENCES users(id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT uq_notification_recipient
                                             UNIQUE (notification_id, user_id),

                                         CONSTRAINT chk_notification_recipient_read_state
                                             CHECK (
                                                 (
                                                     is_read = FALSE
                                                         AND read_at IS NULL
                                                     )
                                                     OR
                                                 (
                                                     is_read = TRUE
                                                         AND read_at IS NOT NULL
                                                     )
                                                 ),

                                         CONSTRAINT chk_notification_recipient_read_time
                                             CHECK (
                                                 read_at IS NULL
                                                     OR read_at >= created_at
                                                 )
);

CREATE INDEX idx_notification_recipients_user_id
    ON notification_recipients(user_id);

CREATE INDEX idx_notification_recipients_notification_id
    ON notification_recipients(notification_id);

CREATE INDEX idx_notification_recipients_user_created_at
    ON notification_recipients(user_id, created_at DESC);

CREATE INDEX idx_notification_recipients_unread
    ON notification_recipients(user_id, created_at DESC)
    WHERE is_read = FALSE;