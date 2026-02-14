CREATE TABLE case_documents (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    sensitivity_level VARCHAR(50) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_case_documents_case
        FOREIGN KEY (case_id)
        REFERENCES cases(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_case_documents_user
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
);

CREATE INDEX idx_case_documents_tenant_case
    ON case_documents (tenant_id, case_id);
