CREATE TABLE user_account (
    id                     UUID PRIMARY KEY,
    username_or_email      VARCHAR(255) NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    failed_login_attempts  INTEGER NOT NULL DEFAULT 0,
    locked                 BOOLEAN NOT NULL DEFAULT FALSE,
    enabled                BOOLEAN NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_account_username_or_email UNIQUE (username_or_email)
);
