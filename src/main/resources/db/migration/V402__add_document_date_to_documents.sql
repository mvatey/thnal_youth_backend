ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS document_date DATE;

UPDATE documents
SET document_date = created_at::date
WHERE document_date IS NULL;

SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'documents'
  AND column_name = 'document_date';