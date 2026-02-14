ALTER TABLE case_documents
ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0,
ADD COLUMN file_hash VARCHAR(128),
ADD COLUMN document_group_id VARCHAR(64),
ADD COLUMN version INT NOT NULL DEFAULT 1;

CREATE INDEX idx_case_documents_active
ON case_documents (tenant_id, case_id, active);

CREATE INDEX idx_case_documents_group
ON case_documents (document_group_id);
