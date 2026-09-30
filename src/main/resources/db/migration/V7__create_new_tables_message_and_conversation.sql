ALTER TABLE users
    ADD COLUMN online_status VARCHAR(50) NULL DEFAULT 'OFFLINE',
    ADD COLUMN last_seen TIMESTAMPTZ NULL;


CREATE TABLE chat_conversations
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    passenger_id UUID        NOT NULL,
    agent_id     UUID NULL,
    status       VARCHAR(30) NOT NULL,
    encrypted    BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_conversation_passenger FOREIGN KEY (passenger_id) REFERENCES users (id),
    CONSTRAINT fk_chat_conversation_agent FOREIGN KEY (agent_id) REFERENCES users (id),
    CONSTRAINT chk_passenger_agent_different CHECK (passenger_id <> agent_id)
);

-- Indexes for chat_conversations
CREATE INDEX idx_chat_conversation_passenger ON chat_conversations (passenger_id);
CREATE INDEX idx_chat_conversation_agent ON chat_conversations (agent_id);
CREATE INDEX idx_chat_conversation_status ON chat_conversations (status);
CREATE INDEX idx_chat_conversation_updated ON chat_conversations (updated_at);

-- 3. Create chat_messages table
CREATE TABLE chat_messages
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    conversation_id UUID         NOT NULL,
    sender_id       UUID         NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    content_iv      VARCHAR(255) NOT NULL,
    cipher_text     TEXT         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at         TIMESTAMPTZ  NOT NULL,
    delivered_at    TIMESTAMPTZ NULL,
    CONSTRAINT fk_chat_message_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);

-- Indexes for chat_messages
CREATE INDEX idx_chat_message_conversation_created ON chat_messages (conversation_id, created_at);
CREATE INDEX idx_chat_message_sender ON chat_messages (sender_id);
CREATE INDEX idx_chat_message_conversation_sender_created ON chat_messages (conversation_id, sender_id, created_at);