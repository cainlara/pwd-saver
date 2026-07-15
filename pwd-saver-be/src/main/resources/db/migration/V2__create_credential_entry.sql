CREATE TABLE credential_entry (
    id               UUID PRIMARY KEY,
    user_account_id  UUID NOT NULL REFERENCES user_account (id),
    status           VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT chk_credential_entry_status CHECK (status IN ('active', 'deleted'))
);

CREATE INDEX idx_credential_entry_owner_status ON credential_entry (user_account_id, status);
