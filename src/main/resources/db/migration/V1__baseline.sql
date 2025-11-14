CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE firms (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    firm_id UUID NOT NULL REFERENCES firms(id),
    email TEXT NOT NULL,
    role TEXT NOT NULL
);

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    firm_id UUID NOT NULL REFERENCES firms(id),
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (firm_id, id)
);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    firm_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    name TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (firm_id, id),
    CONSTRAINT projects_customer_fk FOREIGN KEY (firm_id, customer_id)
        REFERENCES customers (firm_id, id)
);

CREATE TABLE time_entries (
    id UUID PRIMARY KEY,
    firm_id UUID NOT NULL,
    user_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    project_id UUID NOT NULL,
    matter_id UUID NULL,
    narrative TEXT NOT NULL,
    duration_minutes INT NOT NULL CHECK (duration_minutes > 0),
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    approved_at TIMESTAMPTZ NULL,
    approved_by UUID NULL,
    CONSTRAINT time_entries_project_fk FOREIGN KEY (firm_id, project_id)
        REFERENCES projects (firm_id, id),
    CONSTRAINT time_entries_customer_fk FOREIGN KEY (firm_id, customer_id)
        REFERENCES customers (firm_id, id)
);

CREATE TABLE approvals (
    entry_id UUID NOT NULL REFERENCES time_entries(id),
    approver_id UUID NOT NULL REFERENCES user_profiles(id),
    approved_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (entry_id, approver_id)
);

CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    firm_id UUID NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type TEXT NOT NULL,
    event_key TEXT NOT NULL,
    payload_json JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ NULL
);

CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    operation TEXT NOT NULL,
    user_id UUID NOT NULL,
    firm_id UUID NOT NULL,
    key_hash TEXT NOT NULL,
    response_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    UNIQUE (operation, user_id, key_hash)
);

CREATE TABLE integration_configs (
    id UUID PRIMARY KEY,
    firm_id UUID NOT NULL REFERENCES firms(id),
    provider TEXT NOT NULL,
    dek_ciphertext BYTEA NOT NULL,
    refresh_token_ciphertext BYTEA NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_time_entries_firm_status ON time_entries (firm_id, status);
CREATE INDEX idx_time_entries_firm_project ON time_entries (firm_id, project_id);
CREATE INDEX idx_time_entries_firm_owner ON time_entries (firm_id, user_id);

ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE time_entries ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_customers ON customers
    USING (firm_id::text = current_setting('app.current_firm_id', true));

CREATE POLICY tenant_projects ON projects
    USING (firm_id::text = current_setting('app.current_firm_id', true));

CREATE POLICY tenant_entries ON time_entries
    USING (firm_id::text = current_setting('app.current_firm_id', true));

ALTER TABLE customers FORCE ROW LEVEL SECURITY;
ALTER TABLE projects FORCE ROW LEVEL SECURITY;
ALTER TABLE time_entries FORCE ROW LEVEL SECURITY;
