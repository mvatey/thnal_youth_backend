CREATE TABLE activity_attachments
(
    id           BIGSERIAL PRIMARY KEY,

    activity_id  BIGINT      NOT NULL,
    file_id      BIGINT      NOT NULL,

    title        VARCHAR(255),
    description  TEXT,

    sort_order   INTEGER     NOT NULL DEFAULT 0,

    uploaded_by  BIGINT      NOT NULL,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_attachments_activity
        FOREIGN KEY (activity_id)
            REFERENCES activities (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_activity_attachments_file
        FOREIGN KEY (file_id)
            REFERENCES files (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_activity_attachments_uploaded_by
        FOREIGN KEY (uploaded_by)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT uk_activity_attachments_file
        UNIQUE (file_id)
);

CREATE INDEX idx_activity_attachments_activity_id
    ON activity_attachments (activity_id);

CREATE INDEX idx_activity_attachments_uploaded_by
    ON activity_attachments (uploaded_by);

CREATE INDEX idx_activity_attachments_sort_order
    ON activity_attachments (activity_id, sort_order);