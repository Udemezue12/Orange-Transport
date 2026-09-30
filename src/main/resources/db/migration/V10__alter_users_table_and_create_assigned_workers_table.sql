CREATE INDEX IF NOT EXISTS idx_users_status_role ON users (status, role);
CREATE INDEX IF NOT EXISTS idx_users_status_id ON users (status, id);


-- Create the assigned_workers table
CREATE TABLE assigned_workers
(
    id                    UUID        NOT NULL,
    assigned_by_user_id   UUID        NOT NULL,
    worker_user_id        UUID        NOT NULL,
    assigned_service_type VARCHAR(30) NOT NULL,
    assigned_service_id   UUID        NOT NULL,
    assigned_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Primary Key Constraint
    CONSTRAINT pk_assigned_workers PRIMARY KEY (id),

    -- Foreign Key Constraints
    CONSTRAINT fk_assigned_worker_assigned_by
        FOREIGN KEY (assigned_by_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_assigned_worker_worker
        FOREIGN KEY (worker_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_assigned_workers_worker
        UNIQUE (worker_user_id),

    CONSTRAINT uk_assigned_workers_entity
        UNIQUE (assigned_service_type, assigned_service_id)
);

-- Indexes (Excluding worker_user_id since the UNIQUE constraint automatically creates one)
CREATE INDEX idx_assigned_workers_assigned_by
    ON assigned_workers (assigned_by_user_id);

CREATE INDEX idx_assigned_workers_entity
    ON assigned_workers (assigned_service_type, assigned_service_id);
