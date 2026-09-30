-- Migration Script: V15__alter_trips_and_assign_workers.sql

BEGIN;

-- 1. Add foreign key column 'vehicle_loader_profile_id' to 'trips'
ALTER TABLE trips
    ADD COLUMN vehicle_loader_profile_id UUID;

ALTER TABLE trips
    ADD CONSTRAINT fk_trips_vehicle_loader_profile
        FOREIGN KEY (vehicle_loader_profile_id)
            REFERENCES profiles (id);

CREATE INDEX idx_trips_vehicle_loader_profile_id
    ON trips (vehicle_loader_profile_id);


-- 2. Add 'assign_status' to 'assigned_workers' handling existing rows safely

-- Step A: Add column as NULLABLE first
ALTER TABLE assigned_workers
    ADD COLUMN assign_status VARCHAR(30);


UPDATE assigned_workers
SET assign_status = 'ACTIVE'
WHERE assign_status IS NULL;

-- Step C: Enforce NOT NULL constraint
ALTER TABLE assigned_workers
    ALTER COLUMN assign_status SET NOT NULL;

COMMIT;