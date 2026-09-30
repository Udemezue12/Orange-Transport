CREATE TABLE IF NOT EXISTS identity_documents
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(100) NOT NULL,

    document_url VARCHAR(1024),
    document_hash VARCHAR(255),

    document_resource_type VARCHAR(50),
    document_public_id VARCHAR(255),
    document_asset_id VARCHAR(255),

    upload_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    status_changed_at TIMESTAMP WITH TIME ZONE
    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at TIMESTAMP WITH TIME ZONE
    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE,

                             CONSTRAINT chk_identity_documents_upload_status
                             CHECK (
                             upload_status IN (
                             'PENDING',
                             'VERIFIED',
                             'FAILED'
                             )
    ),

    CONSTRAINT chk_identity_documents_verification_status
    CHECK (
              verification_status IN (
              'APPROVED',
              'PENDING',
              'DECLINED'
                                     )
    )
    );


CREATE INDEX IF NOT EXISTS idx_identity_docs_document_type
    ON identity_documents (document_type);

CREATE INDEX IF NOT EXISTS idx_identity_docs_upload_status
    ON identity_documents (upload_status);

CREATE INDEX IF NOT EXISTS idx_identity_docs_verification_status
    ON identity_documents (verification_status);

-- Add new columns to the existing profiles table
ALTER TABLE profiles
    ADD COLUMN IF NOT EXISTS identity_document_id UUID,
    ADD COLUMN IF NOT EXISTS profile_pic_url VARCHAR (1024),
    ADD COLUMN IF NOT EXISTS profile_pic_hash VARCHAR (255),
    ADD COLUMN IF NOT EXISTS profile_pic_resource_type VARCHAR (50),
    ADD COLUMN IF NOT EXISTS profile_pic_public_id VARCHAR (255),
    ADD COLUMN IF NOT EXISTS profile_pic_asset_id VARCHAR (255),
    ADD COLUMN IF NOT EXISTS profile_pic_upload_status VARCHAR (30) DEFAULT 'PENDING';

-- Identity document relationship
ALTER TABLE profiles
    ADD CONSTRAINT fk_profiles_identity_document
        FOREIGN KEY (identity_document_id)
            REFERENCES identity_documents (id)
            ON DELETE SET NULL;


-- User relationship: one profile per user
DO
$$
BEGIN
    IF
NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_profiles_user_id'
    ) THEN
ALTER TABLE profiles
    ADD CONSTRAINT uq_profiles_user_id
        UNIQUE (user_id);
END IF;

    IF
NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_profiles_user'
    ) THEN
ALTER TABLE profiles
    ADD CONSTRAINT fk_profiles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE;
END IF;
END $$;


-- Indexes
CREATE INDEX IF NOT EXISTS idx_profiles_identity_doc_id
    ON profiles (identity_document_id);

CREATE INDEX IF NOT EXISTS idx_profiles_pic_upload_status
    ON profiles (profile_pic_upload_status);