

ALTER TABLE cases
ADD COLUMN matter_type VARCHAR(50),
ADD COLUMN client_name VARCHAR(255),
ADD COLUMN opposing_party_name VARCHAR(255),
ADD COLUMN court_name VARCHAR(255),
ADD COLUMN case_number VARCHAR(255),
ADD COLUMN judge_name VARCHAR(255),
ADD COLUMN stage VARCHAR(50),
ADD COLUMN assigned_advocate VARCHAR(255);




CREATE TABLE case_parties (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    advocate_name VARCHAR(255),
    contact_info VARCHAR(500),
    address VARCHAR(1000),

    CONSTRAINT fk_case_parties_case
        FOREIGN KEY (case_id)
        REFERENCES cases(id)
        ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_case_parties_tenant
    ON case_parties (tenant_id);

CREATE INDEX idx_case_parties_case
    ON case_parties (case_id);

CREATE INDEX idx_case_parties_tenant_case
    ON case_parties (tenant_id, case_id);



CREATE TABLE case_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    description VARCHAR(5000),
    event_date TIMESTAMP,
    next_date TIMESTAMP,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_case_events_case
        FOREIGN KEY (case_id)
        REFERENCES cases(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_case_events_user
        FOREIGN KEY (created_by)
        REFERENCES users(id)
);

-- Indexes for performance
CREATE INDEX idx_case_events_tenant
    ON case_events (tenant_id);

CREATE INDEX idx_case_events_case
    ON case_events (case_id);

CREATE INDEX idx_case_events_tenant_case
    ON case_events (tenant_id, case_id);

CREATE INDEX idx_case_events_event_date
    ON case_events (event_date);



ALTER TABLE case_parties
ADD CONSTRAINT chk_case_party_role
CHECK (role IN (
    'PETITIONER',
    'RESPONDENT',
    'ACCUSED',
    'COMPLAINANT',
    'APPELLANT',
    'DEFENDANT',
    'OTHER'
));

ALTER TABLE case_events
ADD CONSTRAINT chk_case_event_type
CHECK (event_type IN (
    'FILING',
    'HEARING',
    'ORDER',
    'ADJOURNMENT',
    'CLIENT_MEETING',
    'INTERNAL_REVIEW',
    'JUDGMENT',
    'NOTICE',
    'OTHER'
));
