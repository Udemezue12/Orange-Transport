-- 1. Add the 'status' column allowing NULL values initially to handle existing data
ALTER TABLE registration_codes
    ADD COLUMN status VARCHAR(30);

-- 2. Backfill existing rows with the 'INVALID' status
UPDATE registration_codes
SET status = 'INVALID'
WHERE status IS NULL;

-- 3. Enforce the NOT NULL constraint now that all rows are populated
ALTER TABLE registration_codes
    ALTER COLUMN status SET NOT NULL;

-- 4. Create the composite index matching your JPA specification
CREATE INDEX idx_registration_codes_code_role_status
    ON registration_codes (register_code, code_role, status);
