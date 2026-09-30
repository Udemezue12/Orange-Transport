CREATE TABLE oauth_accounts
(
    id               UUID PRIMARY KEY,

    user_id          UUID         NOT NULL,

    provider         VARCHAR(30)  NOT NULL,

    provider_subject VARCHAR(255) NOT NULL,

    created_at       TIMESTAMPTZ  NOT NULL,

    last_login_at    TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_oauth_account_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_oauth_account_provider_subject
        UNIQUE (provider, provider_subject)
);

CREATE INDEX idx_oauth_account_user_id
    ON oauth_accounts (user_id);

CREATE INDEX idx_oauth_account_provider_subject
    ON oauth_accounts (provider, provider_subject);