package com.astrotech.transport.service;

import com.astrotech.transport.dto.response.TransloadingResponse;
import com.astrotech.transport.entities.TransloadingEvent;

import com.astrotech.transport.enums.TransloadingStatus;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.*;
import com.astrotech.transport.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TransloadingEventAndAllocationService {
    private final TransloadingEventRepository transloadingEventRepository;
    private final TransloadingAllocationRepository allocationRepository;
    private final TripVehicleAllocationRepository tripVehicleAllocationRepository;

    @Transactional
    public TransloadingResponse createTransloadingEvent(UUID tripVehicleAllocationId, String incidentDescription, String incidentReason, String locationName) {
        var transloadingMapper = backgroundCreate(tripVehicleAllocationId, incidentDescription, incidentReason, locationName);
        return TransloadingEventMapper.response(transloadingMapper);

    }
    @Transactional
    public void createAllocation(UUID tripVehicleAllocationId, TransloadingEvent transloadingEvent) {
        var tripVehicleAllocation= tripVehicleAllocationRepository.findById(tripVehicleAllocationId).orElse(null);
        if (tripVehicleAllocation == null) {
            return;
        }
        var allocationMapper = TransloadingAllocationMapper.create(transloadingEvent, tripVehicleAllocation);
        allocationRepository.save(allocationMapper);


    }

    @Transactional
    public TransloadingResponse updateTransloadingEventStatus(UUID transloadingId, TransloadingStatus status) {
        var transloading = allocationRepository.findByTransloadingEventId(transloadingId).orElseThrow(() -> new ResourceNotFoundException("Transloading Event doesnt exist'"));
        if (status == TransloadingStatus.INITIATED) {
            throw new BadRequestException("Transloading Event is already initiated");

        }
        transloading.getTransloadingEvent().setStatus(status);
        return TransloadingEventMapper.response(transloading.getTransloadingEvent());

    }
    @Transactional
    public TransloadingEvent backgroundCreate(UUID tripVehicleAllocationId, String incidentDescription, String incidentReason, String locationName) {
        var transloadingMapper = TransloadingEventMapper.create(incidentDescription, incidentReason, locationName);

        var savedTransloading = transloadingEventRepository.save(transloadingMapper);
        createAllocation(tripVehicleAllocationId, savedTransloading);
        return savedTransloading;


    }
}
