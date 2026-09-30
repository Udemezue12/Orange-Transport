CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: trip_vehicle_allocations
CREATE TABLE trip_vehicle_allocations
(
    id                        UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    trip_id                   UUID        NOT NULL,
    vehicle_id                UUID        NOT NULL,
    driver_profile_id         UUID,
    allocation_role           VARCHAR(20) NOT NULL,
    allocation_status         VARCHAR(20) NOT NULL,
    assigned_at               TIMESTAMPTZ NOT NULL,
    released_at               TIMESTAMPTZ, -- Added missing comma here

    CONSTRAINT fk_tva_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT fk_tva_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id) ON DELETE CASCADE,
    CONSTRAINT fk_tva_driver FOREIGN KEY (driver_profile_id) REFERENCES driver_profiles (id) ON DELETE SET NULL
);

CREATE INDEX idx_tva_trip ON trip_vehicle_allocations (trip_id);
CREATE INDEX idx_tva_vehicle ON trip_vehicle_allocations (vehicle_id);
CREATE INDEX idx_tva_driver ON trip_vehicle_allocations (driver_profile_id);
CREATE INDEX idx_tva_trip_status ON trip_vehicle_allocations (trip_id, allocation_status);
CREATE INDEX idx_tva_trip_role_status ON trip_vehicle_allocations (trip_id, allocation_role, allocation_status);


-- Table: passenger_manifests
CREATE TABLE passenger_manifests
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id         UUID         NOT NULL UNIQUE,
    phone_number      VARCHAR(255) NOT NULL,
    address           VARCHAR(255) NOT NULL,
    boarding_point    VARCHAR(255) NOT NULL,
    destination       VARCHAR(255) NOT NULL,
    next_of_kin_name  VARCHAR(255) NOT NULL,
    next_of_kin_phone VARCHAR(255) NOT NULL,

    CONSTRAINT fk_pm_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE
);


-- Table: transloading_events
CREATE TABLE transloading_events
(
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_reason      VARCHAR(255) NOT NULL,
    incident_description TEXT         NOT NULL,
    status               VARCHAR(20)  NOT NULL,
    reported_at          TIMESTAMPTZ  NOT NULL,
    resolved_at          TIMESTAMPTZ,
    location_name        VARCHAR(255) NOT NULL,
    latitude             DOUBLE PRECISION,
    longitude            DOUBLE PRECISION -- Removed trailing comma here
);

CREATE INDEX idx_tle_location_name ON transloading_events (location_name);
CREATE INDEX idx_tle_status ON transloading_events (status);


-- Table: transloading_allocations
CREATE TABLE transloading_allocations
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transloading_event_id UUID        NOT NULL,
    rescue_allocation_id  UUID        NOT NULL,
    transloaded_at        TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_tpa_event FOREIGN KEY (transloading_event_id) REFERENCES transloading_events (id) ON DELETE CASCADE,
    CONSTRAINT fk_tpa_allocation FOREIGN KEY (rescue_allocation_id) REFERENCES trip_vehicle_allocations (id) ON DELETE CASCADE,
    CONSTRAINT uq_tpa_event_allocation UNIQUE (transloading_event_id, rescue_allocation_id)
);

CREATE INDEX idx_tpa_allocation ON transloading_allocations (rescue_allocation_id);
CREATE INDEX idx_tpa_event ON transloading_allocations (transloading_event_id);

