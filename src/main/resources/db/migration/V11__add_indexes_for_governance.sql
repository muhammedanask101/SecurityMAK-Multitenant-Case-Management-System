-- USERS
CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (email);

CREATE INDEX IF NOT EXISTS idx_users_tenant
    ON users (tenant_id);

-- INVITES
CREATE INDEX IF NOT EXISTS idx_invites_token
    ON invites (token);

CREATE INDEX IF NOT EXISTS idx_invites_tenant
    ON invites (tenant_id);

CREATE INDEX IF NOT EXISTS idx_invites_email_tenant
    ON invites (email, tenant_id);

-- EMAIL BANS
CREATE INDEX IF NOT EXISTS idx_email_bans_email_tenant
    ON email_bans (email, tenant_id);

-- AUDIT LOGS
CREATE INDEX IF NOT EXISTS idx_audit_logs_tenant_timestamp
    ON audit_logs (tenant_id, timestamp);

-- CASES
CREATE INDEX IF NOT EXISTS idx_cases_tenant_owner
    ON cases (tenant_id, owner_id);

CREATE INDEX IF NOT EXISTS idx_cases_tenant_created
    ON cases (tenant_id, created_at);

    CREATE INDEX IF NOT EXISTS idx_case_comments_case
    ON case_comments (case_id);

CREATE INDEX IF NOT EXISTS idx_case_comments_case_created
    ON case_comments (case_id, created_at);

CREATE INDEX IF NOT EXISTS idx_case_comments_author
    ON case_comments (author_id);
