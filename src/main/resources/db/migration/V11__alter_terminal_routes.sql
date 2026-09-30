-- Change stop_order from VARCHAR to INTEGER
ALTER TABLE terminal_routes
ALTER COLUMN stop_order TYPE INTEGER
    USING NULLIF(TRIM(stop_order), '')::INTEGER;




-- Prevent the same stop order from being used twice on the same route
ALTER TABLE terminal_routes
    ADD CONSTRAINT uk_route_stop_order
        UNIQUE (route_id, stop_order);