CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";


CREATE TABLE users
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    email          VARCHAR(255) NOT NULL UNIQUE,
    full_name      VARCHAR(255) NOT NULL,
    phone_number   VARCHAR(255),

    oauth_provider VARCHAR(50),
    oauth_subject  VARCHAR(255),
    password       VARCHAR(255) NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    status         VARCHAR(50)  NOT NULL,

    suspended      BOOLEAN      NOT NULL DEFAULT false,
    deleted        BOOLEAN      NOT NULL DEFAULT false,
    verified       BOOLEAN      NOT NULL DEFAULT false,

    verified_at    TIMESTAMPTZ,
    deleted_at     TIMESTAMPTZ,
    suspended_at   TIMESTAMPTZ,
    login_at       TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_users_email
    ON users (email);

CREATE INDEX idx_users_role
    ON users (role);

CREATE UNIQUE INDEX uk_oauth_account
    ON users (oauth_provider, oauth_subject) WHERE oauth_provider IS NOT NULL
      AND oauth_subject IS NOT NULL;


CREATE TABLE blacklisted_tokens
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jti        VARCHAR(255) NOT NULL UNIQUE,
    user_id    VARCHAR(255) NOT NULL,
    token_type VARCHAR(50)  NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_blacklisted_jti ON blacklisted_tokens (jti);



CREATE TABLE driver_profiles
(
    id                          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    license_number              VARCHAR(255) NOT NULL UNIQUE,
    is_active                   BOOLEAN      NOT NULL DEFAULT false,
    image_url                   VARCHAR(255),
    image_hash                  VARCHAR(255),
    resource_type               VARCHAR(255),
    public_id                   VARCHAR(255),
    asset_id                    VARCHAR(255),
    driver_id                   UUID         NOT NULL REFERENCES users (id),
    license_verification_status VARCHAR(50)  NOT NULL,
    image_upload_status         VARCHAR(50)  NOT NULL,
    license_verified            BOOLEAN               DEFAULT false,
    license_verified_at         TIMESTAMPTZ,
    CONSTRAINT uk_driver_profile_license_number UNIQUE (license_number),
    CONSTRAINT uk_driver_profile_asset_id UNIQUE (asset_id)
);


CREATE TABLE idempotency_records
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key UUID         NOT NULL
        CONSTRAINT uk_idempotency_key UNIQUE,
    request_hash    VARCHAR(255) NOT NULL,
    http_method     VARCHAR(20)  NOT NULL,
    request_path    VARCHAR(255) NOT NULL,
    resource_id     VARCHAR(255),
    resource_type   VARCHAR(255),
    response_class  VARCHAR(255),
    status_code     INT,
    response_body   TEXT,
    response_header TEXT,
    content_type    VARCHAR(255),
    error_message   TEXT,
    exception_class VARCHAR(255),
    status          VARCHAR(50)  NOT NULL,
    locked_at       TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_request_hash ON idempotency_records (request_hash);
CREATE INDEX idx_created_at ON idempotency_records (created_at);


CREATE TABLE notifications
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL REFERENCES users (id),
    type           VARCHAR(50)  NOT NULL,
    title          VARCHAR(200) NOT NULL,
    message        TEXT         NOT NULL,
    is_read        BOOLEAN      NOT NULL DEFAULT false,
    action_url     VARCHAR(255),
    reference_id   UUID,
    reference_type VARCHAR(50)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    read_at        TIMESTAMPTZ
);

CREATE INDEX idx_notification_user_created ON notifications (user_id, created_at);
CREATE INDEX idx_notification_user_read ON notifications (user_id, is_read);
CREATE INDEX idx_notification_type ON notifications (type);


CREATE TABLE profiles
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id)
);


CREATE TABLE terminals
(
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                   VARCHAR(255) NOT NULL,
    city                   VARCHAR(255) NOT NULL,
    address                VARCHAR(255) NOT NULL,
    state                  VARCHAR(255) NOT NULL,
    terminal_supervisor_id UUID         NOT NULL UNIQUE REFERENCES users (id)
);


CREATE TABLE routes
(
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin_terminal_id         UUID NOT NULL REFERENCES terminals (id),
    destination_terminal_id    UUID NOT NULL REFERENCES terminals (id),
    distance_km                DOUBLE PRECISION,
    estimated_duration_minutes INT,
    CONSTRAINT uk_route_origin_destination UNIQUE (origin_terminal_id, destination_terminal_id)
);


CREATE TABLE route_fares
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id       UUID           NOT NULL REFERENCES routes (id),
    vehicle_class  VARCHAR(50)    NOT NULL,
    amount         NUMERIC(12, 2) NOT NULL,
    effective_from TIMESTAMPTZ    NOT NULL,
    effective_to   TIMESTAMPTZ    NOT NULL,
    active         BOOLEAN        NOT NULL,
    CONSTRAINT uk_route_fare_class UNIQUE (route_id, vehicle_class)
);


CREATE TABLE terminal_routes
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    terminal_id           UUID NOT NULL REFERENCES terminals (id),
    route_id              UUID NOT NULL REFERENCES routes (id),
    stop_order            VARCHAR(255),
    stop_order_created_at TIMESTAMPTZ,
    CONSTRAINT uk_terminal_route UNIQUE (terminal_id, route_id)
);


CREATE TABLE vehicles
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    registration_number VARCHAR(255) NOT NULL UNIQUE,
    capacity            INT          NOT NULL,
    vehicle_type        VARCHAR(50)  NOT NULL,
    brand               VARCHAR(50)  NOT NULL,
    status              VARCHAR(50)  NOT NULL,
    fuel_type           VARCHAR(50)  NOT NULL,
    transmission        VARCHAR(50)  NOT NULL,
    image_upload_status VARCHAR(50)  NOT NULL,
    manufacture_year    INT          NOT NULL,
    color               VARCHAR(50)  NOT NULL,
    chassis_number      VARCHAR(255) NOT NULL,
    engine_number       VARCHAR(255) NOT NULL,
    vin                 VARCHAR(255) UNIQUE,
    model               VARCHAR(255) NOT NULL,
    vehicle_class       VARCHAR(50)  NOT NULL,
    thumb_nail_url      VARCHAR(255)
);


CREATE TABLE vehicle_images
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    image_hash    VARCHAR(255),
    resource_type VARCHAR(255),
    image_url     VARCHAR(255),
    public_id     VARCHAR(255),
    asset_id      VARCHAR(255),
    thumbnail_url VARCHAR(255),
    vehicle_id    UUID NOT NULL REFERENCES vehicles (id),
    CONSTRAINT uk_vehicle_asset UNIQUE (vehicle_id, asset_id)
);


CREATE TABLE seats
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    position    VARCHAR(50) NOT NULL,
    status      VARCHAR(50) NOT NULL,
    seat_number VARCHAR(50) NOT NULL,
    row_number  INT         NOT NULL,
    vehicle_id  UUID        NOT NULL REFERENCES vehicles (id),
    CONSTRAINT uk_seat_vehicle_number UNIQUE (vehicle_id, seat_number)
);


CREATE TABLE trips
(
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_code                VARCHAR(255) NOT NULL UNIQUE,
    scheduled_departure_time TIMESTAMPTZ  NOT NULL,
    scheduled_arrival_time   TIMESTAMPTZ  NOT NULL,
    boarding_time            TIMESTAMPTZ  NOT NULL,
    booking_cutoff           TIMESTAMPTZ  NOT NULL,
    actual_departure_time    TIMESTAMPTZ,
    actual_arrival_time      TIMESTAMPTZ,
    status                   VARCHAR(50)  NOT NULL,
    delay_reason             VARCHAR(255),
    route_id                 UUID         NOT NULL REFERENCES routes (id),
    vehicle_id               UUID         NOT NULL REFERENCES vehicles (id),
    driver_profile_id        UUID REFERENCES driver_profiles (id),
    created_by               UUID REFERENCES users (id),
    version                  BIGINT
);

CREATE INDEX idx_trips_route_scheduled_departure ON trips (route_id, scheduled_departure_time);
CREATE INDEX idx_trips_status ON trips (status);
CREATE INDEX idx_trips_code ON trips (trip_code);

-- 15. BOOKING SESSIONS TABLE
CREATE TABLE booking_sessions
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference    VARCHAR(30)    NOT NULL UNIQUE,
    trip_id      UUID           NOT NULL REFERENCES trips (id),
    passenger_id UUID REFERENCES users (id),
    status       VARCHAR(50)    NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    expires_at   TIMESTAMPTZ    NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_booking_session_reference ON booking_sessions (reference);
CREATE INDEX idx_booking_session_status ON booking_sessions (status);
CREATE INDEX idx_booking_session_expires ON booking_sessions (expires_at);

-- 16. TRIP SEAT RESERVATIONS TABLE
CREATE TABLE trip_seat_reservations
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id            UUID        NOT NULL REFERENCES trips (id),
    seat_id            UUID        NOT NULL REFERENCES seats (id),
    passenger_name VARCHAR(150) NOT NULL,
    booking_session_id UUID REFERENCES booking_sessions (id),
    status             VARCHAR(50) NOT NULL,
    expires_at         TIMESTAMPTZ,
    reserved_at        TIMESTAMPTZ NOT NULL,
    checked_in_at      TIMESTAMPTZ,
    version            BIGINT,
    CONSTRAINT uk_trip_seat UNIQUE (trip_id, seat_id)
);

CREATE INDEX idx_trip_reservation_trip ON trip_seat_reservations (trip_id);
CREATE INDEX idx_trip_reservation_status ON trip_seat_reservations (status);
CREATE INDEX idx_trip_reservation_expires ON trip_seat_reservations (expires_at);

CREATE TABLE payments
(
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    generated_reference             VARCHAR(255)   NOT NULL UNIQUE,
    payment_provider_reference      VARCHAR(255) UNIQUE,
    payment_provider_transaction_id VARCHAR(255) UNIQUE,
    currency                        VARCHAR(10)    NOT NULL,
    payment_channel                 VARCHAR(255),
    payment_method                  VARCHAR(50)    NOT NULL,
    status                          VARCHAR(50)    NOT NULL,
    amount                          NUMERIC(12, 2) NOT NULL,
    processed                       BOOLEAN          DEFAULT false,
    payer_id                    UUID           NOT NULL REFERENCES users (id),
    booking_session_id              UUID           NOT NULL REFERENCES booking_sessions (id),
    paid_at                         TIMESTAMPTZ,
    verified_at                     TIMESTAMPTZ,
    failed_at                       TIMESTAMPTZ,
    refunded_at                     TIMESTAMPTZ,
    created_at                      TIMESTAMPTZ    NOT NULL,
    version                         BIGINT
);

CREATE INDEX idx_payment_reference ON payments (generated_reference);
CREATE INDEX idx_payment_provider_reference ON payments (payment_provider_reference);
CREATE INDEX idx_payment_booking ON payments (booking_session_id);
CREATE INDEX idx_payment_status ON payments (status);

CREATE TABLE tickets
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_number  VARCHAR(255)   NOT NULL UNIQUE,
    booking_id     UUID           NOT NULL REFERENCES booking_sessions (id),
    passenger_id   UUID           NOT NULL REFERENCES users (id),
    payment_id     UUID           NOT NULL,
    price          NUMERIC(12, 2) NOT NULL,
    status         VARCHAR(50)    NOT NULL,
    reservation_id UUID           NOT NULL UNIQUE REFERENCES trip_seat_reservations (id),
    issued_at      TIMESTAMPTZ    NOT NULL,
    checked_in_at  TIMESTAMPTZ,

    CONSTRAINT fk_ticket_payment
        FOREIGN KEY (payment_id)
            REFERENCES payments (id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_ticket_payment
    ON tickets(payment_id);
CREATE INDEX idx_ticket_booking ON tickets (booking_id);
CREATE INDEX idx_ticket_trip_reservation ON tickets (reservation_id);


CREATE TABLE ticket_pdfs
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id     UUID          NOT NULL UNIQUE REFERENCES tickets (id),
    asset_id      VARCHAR(255)  NOT NULL,
    resource_type VARCHAR(255)  NOT NULL,
    public_id     VARCHAR(255)  NOT NULL UNIQUE,
    secure_url    VARCHAR(1000) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_ticket_pdf_ticket_id ON ticket_pdfs (ticket_id);
CREATE INDEX idx_ticket_pdf_created_at ON ticket_pdfs (created_at);


