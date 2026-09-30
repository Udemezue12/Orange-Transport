package com.astrotech.transport.events;

import java.util.List;
import java.util.UUID;

public record UpdateVehicleSeat(
        UUID vehicleId,
        Integer vehicleCapacity,
        List<Integer> seatsPerRow
) {
}
