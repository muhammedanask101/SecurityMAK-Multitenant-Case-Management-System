CREATE TABLE case_assignments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_case_assignments_case
        FOREIGN KEY (case_id)
        REFERENCES cases(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_case_assignments_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_case_assignments_tenant_case
    ON case_assignments (tenant_id, case_id);

CREATE INDEX idx_case_assignments_user
    ON case_assignments (user_id);
