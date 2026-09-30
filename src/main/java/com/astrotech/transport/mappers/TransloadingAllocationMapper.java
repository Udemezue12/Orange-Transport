package com.astrotech.transport.mappers;

import com.astrotech.transport.entities.TransloadingAllocation;
import com.astrotech.transport.entities.TransloadingEvent;
import com.astrotech.transport.entities.TripVehicleAllocation;

import java.time.Instant;

public class TransloadingAllocationMapper {
    public static TransloadingAllocation create(TransloadingEvent transloadingEvent, TripVehicleAllocation tripVehicleAllocation){
        return TransloadingAllocation.builder()
                .rescueAllocation(tripVehicleAllocation)
                .rescueAllocation(tripVehicleAllocation)
                .transloadedAt(Instant.now())
                .build();
    }
    
}
