CREATE TABLE email_bans (
    id BIGSERIAL PRIMARY KEY,

    email VARCHAR(255) NOT NULL,
    tenant_id BIGINT NOT NULL,
    banned_by_id BIGINT NOT NULL,

    banned_at TIMESTAMP NOT NULL,
    reason TEXT,

    CONSTRAINT fk_email_ban_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id),

    CONSTRAINT fk_email_ban_banned_by
        FOREIGN KEY (banned_by_id) REFERENCES users(id),

    CONSTRAINT uq_email_tenant UNIQUE (email, tenant_id)
);