-- Add unique constraint per case (not global)

ALTER TABLE case_documents
ADD CONSTRAINT uk_case_document_hash_per_case
UNIQUE (case_id, file_hash);
