package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.TransloadingResponse;
import com.astrotech.transport.entities.TransloadingAllocation;
import com.astrotech.transport.entities.TransloadingEvent;
import com.astrotech.transport.entities.TripVehicleAllocation;
import com.astrotech.transport.enums.TransloadingStatus;

import java.time.Instant;

public class TransloadingEventMapper {
    public static TransloadingEvent create(String incidentDescription, String incidentReason, String locationName){
        return TransloadingEvent.builder()
                .incidentDescription(incidentDescription)
                .incidentReason(incidentReason)
                .locationName(locationName)
                .reportedAt(Instant.now())
                .status(TransloadingStatus.INITIATED)
                .build();
    }
    public static TransloadingResponse response(TransloadingEvent transloadingEvent){
        return new TransloadingResponse(
                transloadingEvent.getId(),
                transloadingEvent.getIncidentReason(),
                transloadingEvent.getIncidentDescription(),
                transloadingEvent.getStatus(),
                transloadingEvent.getLocationName(),
                transloadingEvent.getReportedAt(),
                transloadingEvent.getResolvedAt()

        );

    }
}

