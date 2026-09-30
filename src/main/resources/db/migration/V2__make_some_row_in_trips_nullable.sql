ALTER TABLE trips
    ALTER COLUMN actual_departure_time DROP NOT NULL,
    ALTER COLUMN actual_arrival_time DROP NOT NULL;