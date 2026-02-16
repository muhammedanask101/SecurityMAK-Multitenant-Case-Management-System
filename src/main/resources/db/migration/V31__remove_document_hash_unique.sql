-- Remove old full unique constraint
ALTER TABLE case_documents
DROP CONSTRAINT IF EXISTS uk_case_document_hash_per_case;

-- Create partial unique index (only active documents must be unique)
CREATE UNIQUE INDEX IF NOT EXISTS uk_case_document_hash_active
ON case_documents(case_id, file_hash)
WHERE active = true;
