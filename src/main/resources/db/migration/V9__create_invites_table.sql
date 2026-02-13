CREATE TABLE invites (
    id BIGSERIAL PRIMARY KEY,

    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,

    tenant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    clearance_level VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    created_by_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,

    terminated_at TIMESTAMP,
    terminated_by_id BIGINT,

    registered_at TIMESTAMP,
    approved_at TIMESTAMP,

    CONSTRAINT fk_invite_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id),

    CONSTRAINT fk_invite_role
        FOREIGN KEY (role_id) REFERENCES roles(id),

    CONSTRAINT fk_invite_created_by
        FOREIGN KEY (created_by_id) REFERENCES users(id),

    CONSTRAINT fk_invite_terminated_by
        FOREIGN KEY (terminated_by_id) REFERENCES users(id),

    CONSTRAINT uq_invite_email_tenant_status
        UNIQUE (email, tenant_id, status)
);