package com.astrotech.transport.events;

import java.util.List;
import java.util.UUID;

public record CreateSeat(
        UUID vehicleId,
        Integer capacity,
        List<Integer> seatsPerRow) {
}
