--43. documents
CREATE TABLE documents (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                           document_type_id SMALLINT NOT NULL,
                           file_id BIGINT NOT NULL,

                           title VARCHAR(255) NOT NULL,
                           description TEXT,

                           branch_id BIGINT,
                           member_id BIGINT,
                           activity_id BIGINT,

                           uploaded_by BIGINT NOT NULL,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_document_type
                               FOREIGN KEY (document_type_id)
                                   REFERENCES document_types(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_document_file
                               FOREIGN KEY (file_id)
                                   REFERENCES files(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_document_branch
                               FOREIGN KEY (branch_id)
                                   REFERENCES branches(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_document_member
                               FOREIGN KEY (member_id)
                                   REFERENCES members(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_document_activity
                               FOREIGN KEY (activity_id)
                                   REFERENCES activities(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT fk_document_uploaded_by
                               FOREIGN KEY (uploaded_by)
                                   REFERENCES users(id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT chk_document_title
                               CHECK (BTRIM(title) <> ''),

                           CONSTRAINT chk_document_owner
                               CHECK (
                                   (
                                       CASE WHEN branch_id IS NOT NULL THEN 1 ELSE 0 END
                                           +
                                       CASE WHEN member_id IS NOT NULL THEN 1 ELSE 0 END
                                           +
                                       CASE WHEN activity_id IS NOT NULL THEN 1 ELSE 0 END
                                       ) <= 1
                                   )
);

CREATE INDEX idx_documents_type_id
    ON documents(document_type_id);

CREATE INDEX idx_documents_branch_id
    ON documents(branch_id);

CREATE INDEX idx_documents_member_id
    ON documents(member_id);

CREATE INDEX idx_documents_activity_id
    ON documents(activity_id);

CREATE INDEX idx_documents_uploaded_by
    ON documents(uploaded_by);

CREATE INDEX idx_documents_created_at
    ON documents(created_at DESC);