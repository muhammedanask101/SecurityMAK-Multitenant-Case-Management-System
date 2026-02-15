ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS file_size BIGINT;

ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS file_hash VARCHAR(64);

ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS document_group_id VARCHAR(100);

ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;

ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;


-- Backfill safety for existing rows
UPDATE case_documents
SET version = 1
WHERE version IS NULL;

UPDATE case_documents
SET active = TRUE
WHERE active IS NULL;

-- Enforce NOT NULL after backfill
ALTER TABLE case_documents
ALTER COLUMN file_size SET NOT NULL;

ALTER TABLE case_documents
ALTER COLUMN file_hash SET NOT NULL;

ALTER TABLE case_documents
ALTER COLUMN document_group_id SET NOT NULL;

ALTER TABLE case_documents
ALTER COLUMN version SET NOT NULL;

ALTER TABLE case_documents
ALTER COLUMN active SET NOT NULL;


-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_case_documents_group
ON case_documents (tenant_id, document_group_id);

CREATE INDEX IF NOT EXISTS idx_case_documents_hash
ON case_documents (tenant_id, file_hash);

CREATE INDEX IF NOT EXISTS idx_case_documents_active
ON case_documents (tenant_id, case_id, active);
