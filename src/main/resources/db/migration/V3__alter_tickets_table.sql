ALTER TABLE tickets
    ADD COLUMN verification_token UUID NOT NULL DEFAULT gen_random_uuid();