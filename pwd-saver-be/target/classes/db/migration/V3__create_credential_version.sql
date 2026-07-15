CREATE TABLE credential_version (
    id                   UUID PRIMARY KEY,
    credential_entry_id  UUID NOT NULL REFERENCES credential_entry (id),
    username             VARCHAR(255) NOT NULL,
    encrypted_password   TEXT NOT NULL,
    url                  VARCHAR(2048),
    description          VARCHAR(1000),
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_credential_version_entry_created ON credential_version (credential_entry_id, created_at);
