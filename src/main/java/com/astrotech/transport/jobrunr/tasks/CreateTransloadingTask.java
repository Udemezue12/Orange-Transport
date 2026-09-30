package com.astrotech.transport.jobrunr.tasks;

import com.astrotech.transport.service.TransloadingEventAndAllocationService;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTransloadingTask {
    private final TransloadingEventAndAllocationService eventAndAllocationService;

    @Job(name = "create-transloading-event-and-allocation", retries = 3)
    public void create(UUID tripVehicleAllocationId, String incidentDescription, String incidentReason, String locationName){
        eventAndAllocationService.backgroundCreate(tripVehicleAllocationId, incidentDescription, incidentReason, locationName);
    }
}
