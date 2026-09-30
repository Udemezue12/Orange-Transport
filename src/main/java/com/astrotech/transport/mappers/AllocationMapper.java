package com.astrotech.transport.mappers;

import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.*;

import java.time.Instant;

public class AllocationMapper {
    public static TripVehicleAllocation mapToTripVehicleAllocation(Trip trip, Vehicle vehicle, DriverProfile driver, AllocationRole allocationRole, AllocationStatus allocationStatus) {
        return TripVehicleAllocation.builder()
                .trip(trip)
                .vehicle(vehicle)
                .driverProfile(driver)
                .role(allocationRole)
                .status(allocationStatus)
                .assignedAt(Instant.now())
                .build();
    }
}
