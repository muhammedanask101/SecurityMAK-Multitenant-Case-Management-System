ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS file_size BIGINT NOT NULL DEFAULT 0;

ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS file_hash VARCHAR(128);

ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS document_group_id VARCHAR(64);

ALTER TABLE case_documents
ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 1;

CREATE INDEX IF NOT EXISTS idx_case_documents_active
ON case_documents (tenant_id, case_id, active);

CREATE INDEX IF NOT EXISTS idx_case_documents_group
ON case_documents (document_group_id);

CREATE INDEX IF NOT EXISTS idx_case_documents_tenant_case
ON case_documents (tenant_id, case_id);
