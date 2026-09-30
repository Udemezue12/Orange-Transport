package com.astrotech.transport.jobrunr.tasks;

import com.astrotech.transport.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatTask {

    private final SeatService seatService;

    @Job(name = "Create Vehicle seats", retries = 3)
    public void create(
            String vehicleId,
            Integer capacity,
            List<Integer> seatsPerRow
    ) {

        seatService.createVehicleSeat(UUID.fromString(vehicleId), capacity, seatsPerRow);

    }
    @Job(name = "Update Vehicle seats", retries = 3)
    public void update(
            UUID vehicleId,
            Integer capacity,
            List<Integer> seatsPerRow
    ) {

        seatService.updateVehicleSeats(vehicleId, capacity, seatsPerRow);

    }

}
