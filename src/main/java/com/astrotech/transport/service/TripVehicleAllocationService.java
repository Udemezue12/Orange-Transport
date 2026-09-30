package com.astrotech.transport.service;

import com.astrotech.transport.dto.request.RescueAndTransloadRequest;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.events.CreateTransloadingEvent;
import com.astrotech.transport.exceptions.*;
import com.astrotech.transport.mappers.AllocationMapper;
import com.astrotech.transport.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TripVehicleAllocationService {

    private static final Set<VehicleStatus> ACTIVE_STATUSES =
            EnumSet.of(
                    VehicleStatus.ON_TRIP,
                    VehicleStatus.AVAILABLE,
                    VehicleStatus.ASSIGNED,
                    VehicleStatus.UNLOADING
            );


    private final VehicleRepository vehicleRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final TripVehicleAllocationRepository allocationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public TripVehicleAllocation transloadVehicle(
            RescueAndTransloadRequest request
    ) {

        var currentAllocation = allocationRepository
                .findByTripAndRoleAndStatus(
                        request.tripId(),
                        AllocationRole.PRIMARY,
                        AllocationStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active primary vehicle allocation not found"
                        )
                );

        var trip = currentAllocation.getTrip();

        var rescueVehicle = vehicleRepository
                .findByIdForUpdate(request.vehicleId(), ACTIVE_STATUSES)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found or cannot participate in rescue"
                        )
                );

        validateVehicleForRescue(
                trip,
                rescueVehicle
        );

        DriverProfile rescueDriver;

        if (rescueVehicle.getStatus() == VehicleStatus.ON_TRIP) {

            var rescueAllocation = allocationRepository
                    .findActivePrimaryAllocationForVehicle(
                            rescueVehicle.getId(),
                            ACTIVE_STATUSES,
                            AllocationRole.PRIMARY,
                            AllocationStatus.ACTIVE
                    )
                    .orElseThrow(() ->
                            new BadRequestException(
                                    "Vehicle is marked ON_TRIP but has no active primary trip"
                            )
                    );

            rescueDriver = rescueAllocation.getTrip().getDriver();

            if (rescueDriver == null) {
                throw new BadRequestException(
                        "Vehicle's current trip has no assigned driver"
                );
            }

        } else {
            var driverRequest = request.driverProfileId();

            if ( driverRequest == null) {
                throw new BadRequestException(
                        "Driver is required for this rescue vehicle"
                );
            }

            rescueDriver = driverProfileRepository
                    .findByIdForUpdate(driverRequest)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Driver profile not found"
                            )
                    );

            if (!rescueDriver.isActive()) {
                throw new BadRequestException(
                        "Driver is not active"
                );
            }
        }

        trip.setStatus(TripStatus.TRANSSHIPMENT);

        currentAllocation.setStatus(AllocationStatus.DISABLED);
        currentAllocation.setReleasedAt(Instant.now());

        var result = createAllocationVehicle(
                trip,
                rescueVehicle,
                rescueDriver,
                AllocationRole.RESCUE,
                AllocationStatus.DISPATCHED
        );
        if (result != null) {
            applicationEventPublisher.publishEvent(
                    new CreateTransloadingEvent(result.getId(),
                            request.incidentDescription(), request.incidentReason(), request.locationName()));
        }
        return result;
    }

    @Transactional
    public TripVehicleAllocation createAllocationVehicle(Trip trip, Vehicle vehicle, DriverProfile rescueDriver, AllocationRole allocationRole, AllocationStatus allocationStatus) {
        var allocation = AllocationMapper.mapToTripVehicleAllocation(trip, vehicle, rescueDriver, allocationRole, allocationStatus);

        return allocationRepository.save(allocation);
    }


    private void validateVehicleForRescue(
            Trip trip,
            Vehicle vehicle
    ) {

        if (vehicle.getId().equals(trip.getVehicle().getId())) {
            throw new BadRequestException(
                    "The primary vehicle cannot be assigned as a rescue vehicle"
            );
        }

        if (!vehicle.getStatus().canParticipateInTransloading()) {
            throw new BadRequestException(
                    "Vehicle cannot participate in emergency transloading"
            );
        }
    }


}
