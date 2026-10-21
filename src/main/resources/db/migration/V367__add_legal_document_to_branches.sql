-- A branch that has been registered by law may carry the registration
-- document (optional). Only the file reference is stored; the file itself
-- lives in the files table like every other attachment.

ALTER TABLE branches
    ADD COLUMN IF NOT EXISTS legal_document_file_id BIGINT
        REFERENCES files (id) ON DELETE SET NULL;
