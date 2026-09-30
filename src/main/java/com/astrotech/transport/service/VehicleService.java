package com.astrotech.transport.service;

import com.astrotech.transport.core.GenerateVehicleRegistrationNumber;
import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.customCache.CustomCacheable;

import com.astrotech.transport.dto.request.VehicleImageRequest;
import com.astrotech.transport.dto.request.VehicleRequest;
import com.astrotech.transport.dto.request.VehicleUpdateRequest;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.VehicleResponse;
import com.astrotech.transport.dto.response.VehicleWithImagesAndSeatsResponse;
import com.astrotech.transport.dto.response.VehicleWithSeatsResponse;
import com.astrotech.transport.entities.Vehicle;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.VehicleClass;
import com.astrotech.transport.events.*;
import com.astrotech.transport.exceptions.*;
import com.astrotech.transport.mappers.VehicleMapper;
import com.astrotech.transport.repositories.VehicleImagesRepository;
import com.astrotech.transport.repositories.VehicleRepository;
import com.astrotech.transport.validators.role.RoleRequired;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final VehicleImagesRepository vehicleImagesRepository;
    private final GenerateVehicleRegistrationNumber generateVehicleRegistrationNumber;


    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @CacheEvict(value = "vehicle-lists", allEntries = true)
    public VehicleResponse createVehicle(VehicleRequest request) {


        var trimmedChassisNumber = TrimWhiteSpace.trimWhiteSpace(request.chassisNumber());
        var trimmedEngineNumber = TrimWhiteSpace.trimWhiteSpace(request.engineNumber());
        var trimmedVin = TrimWhiteSpace.trimWhiteSpace(request.vin());

        var existingVehicles = vehicleRepository.findExistingFields(trimmedChassisNumber, trimmedEngineNumber, trimmedVin);

        if (!existingVehicles.isEmpty()) {
            if (existingVehicles.stream().anyMatch(u -> trimmedEngineNumber.equalsIgnoreCase(u.getEngineNumber()))) {
                throw new BadRequestException("Engine Number already exists");
            }

            if (existingVehicles.stream().anyMatch(u -> trimmedChassisNumber.equals(u.getChassisNumber()))) {
                throw new BadRequestException("Chassis Number already exists");
            }
            if (existingVehicles.stream().anyMatch(u -> trimmedVin.equals(u.getVin()))) {
                throw new BadRequestException("Vin number already exists");
            }
        }
        var nextNumber = getNextNumber(request.vehicleClass());
        var regNumber = generateVehicleRegistrationNumber.getNextId(request.vehicleClass().name(), nextNumber);


        var vehicle = VehicleMapper.createVehicle(request, regNumber);
        var savedVehicle = vehicleRepository.save(vehicle);
        saveVehicleImages(request, savedVehicle);
        eventPublisher.publishEvent(new CreateSeat(savedVehicle.getId(), request.capacity(), request.seatsPerRow()));
        return VehicleMapper.toResponse(savedVehicle);


    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicles", key = "#vehicleId"),
            @CacheEvict(value = "vehicle-details", key = "#vehicleId"),
            @CacheEvict(value = "vehicle-lists", allEntries = true)
    })
    public VehicleResponse updateVehicle(UUID vehicleId, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with ID: " + vehicleId));


        var trimmedModel = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.model(), false);
        var trimmedChassisNumber = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.chassisNumber(), true);
        var trimmedEngineNumber = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.engineNumber(), true);
        var trimmedVin = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.vin(), true);


        if (trimmedModel != null && !trimmedModel.isBlank() && !trimmedModel.equals(vehicle.getModel())) {
            vehicle.setModel(trimmedModel);
        }

        if (request.brand() != null && !request.brand().equals(vehicle.getBrand())) {
            vehicle.setBrand(request.brand());
        }
        if (request.vehicleType() != null && !request.vehicleType().equals(vehicle.getVehicleType())) {
            vehicle.setVehicleType(request.vehicleType());
        }
        if (request.fuelType() != null && !request.fuelType().equals(vehicle.getFuelType())) {
            vehicle.setFuelType(request.fuelType());
        }
        if (request.transmissionType() != null && !request.transmissionType().equals(vehicle.getTransmission())) {
            vehicle.setTransmission(request.transmissionType());
        }
        if (request.manufactureYear() != null && !request.manufactureYear().equals(vehicle.getManufactureYear())) {
            vehicle.setManufactureYear(request.manufactureYear());
        }
        if (request.vehicleColor() != null) {
            vehicle.setColor(request.vehicleColor());
        }
        if (request.vehicleStatus() != null) {
            vehicle.setStatus(request.vehicleStatus());
        }
        if (request.vehicleClass() != null) {
            vehicle.setVehicleClass(request.vehicleClass());
        }
        if (trimmedChassisNumber != null && !trimmedChassisNumber.equals(vehicle.getChassisNumber())) {
            if (vehicleRepository.existsByChassisNumber(trimmedChassisNumber)) {
                throw new ConflictException("Vehicle Chassis number already exists.");
            }
            vehicle.setChassisNumber(trimmedChassisNumber);
        }
        if (trimmedEngineNumber != null && !trimmedEngineNumber.equals(vehicle.getEngineNumber())) {

            if (vehicleRepository.existsByEngineNumber(trimmedEngineNumber)) {
                throw new ConflictException("Vehicle Engine number already exists.");
            }
            vehicle.setEngineNumber(trimmedEngineNumber);
        }
        if (trimmedVin != null && !trimmedVin.equals(vehicle.getVin())) {

            if (vehicleRepository.existsByVin(trimmedVin)) {
                throw new ConflictException("Vehicle Vin number already exists.");
            }
            vehicle.setVin(trimmedVin);
        }
        if (request.vehicleImageId() != null
                && request.assetId() != null
                && !request.assetId().isBlank()
                && request.publicId() != null
                && !request.publicId().isBlank()) {

            var replacement = new VehicleImageReplacement(
                    request.vehicleImageId(),
                    request.assetId(),
                    request.publicId()
            );

            eventPublisher.publishEvent(
                    new VehicleImageUpdateRequest(
                            vehicleId,
                            List.of(replacement)
                    )
            );
        }


        if (request.capacity() != null && request.seatsPerRow() != null && !request.seatsPerRow().isEmpty()) {
            eventPublisher.publishEvent(new UpdateVehicleSeat(vehicle.getId(), request.capacity(), request.seatsPerRow()));
        }

        var savedVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(savedVehicle);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicles", key = "#vehicleId"),
            @CacheEvict(value = "vehicle-details", key = "#vehicleId"),
            @CacheEvict(value = "vehicle-lists", allEntries = true)
    })
    public void updateVehicleImageStatus(UUID vehicleId, ImageUploadStatus uploadStatus) {
        var vehicle = getVehicle(vehicleId);
        vehicle.setUploadStatus(uploadStatus);
        vehicleRepository.save(vehicle);

    }


    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "vehicle-details",
            key = "'images-seats-' + #vehicleId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public VehicleWithImagesAndSeatsResponse getVehicleWithImagesAndSeats(UUID vehicleId) {

        var vehicle = vehicleRepository.findByIdWithSeats(vehicleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found"
                        )
                );

        var images = vehicleImagesRepository.findByVehicleId(vehicleId);

        return VehicleMapper.toVehicleResponse(vehicle, images);
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "vehicle-details",
            key = "'seats-' + #vehicleId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public VehicleWithSeatsResponse getVehicleWithSeats(UUID vehicleId) {

        var vehicle = vehicleRepository.findByIdWithSeats(vehicleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle not found"
                        )
                );


        return VehicleMapper.toVehicleWithSeatResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicle(UUID vehicleId) {


        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "vehicle-lists",
            key = "'p-' + #page + '-s-' + #size + '-sort-' + #sortBy",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<VehicleResponse> getAllVehicles(int page, int size, String sortBy) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Vehicle.class, true);
        var result = vehicleRepository.findAll(pageable);

        var content = result
                .getContent()
                .stream()
                .map(VehicleMapper::toResponse)
                .toList();
        return new SliceResponse<>(
                content,
                page,
                size,
                result.hasNext(),
                result.hasPrevious()
        );
    }

    private int getNextNumber(VehicleClass vehicleClass) {
        var lastVehicle = vehicleRepository
                .findTopByVehicleClassOrderByRegistrationNumberDesc(vehicleClass);

        return lastVehicle
                .map(vehicle -> generateVehicleRegistrationNumber.extractNumber(vehicle.getRegistrationNumber()) + 1)
                .orElse(1);
    }
    private void saveVehicleImages(VehicleRequest request, Vehicle savedVehicle) {
        if (request.images() != null && !request.images().isEmpty()) {
            for (VehicleImageRequest img : request.images()) {
                if (img.assetId() != null && img.publicId() != null) {
                    eventPublisher.publishEvent(
                            new VehicleImageUploadRequest(
                                    savedVehicle.getId().toString(),
                                    img.publicId(),
                                    img.assetId()
                            )
                    );
                }
            }
        }
    }


}
