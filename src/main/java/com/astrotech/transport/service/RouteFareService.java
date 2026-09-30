package com.astrotech.transport.service;


import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.RouteFareRequest;
import com.astrotech.transport.dto.request.RouteFareUpdateRequest;
import com.astrotech.transport.dto.response.RouteFareResponse;
import com.astrotech.transport.dto.response.SimpleRouteFareResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.entities.RouteFare;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.VehicleClass;
import com.astrotech.transport.exceptions.ConflictException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.RouteFareMapper;
import com.astrotech.transport.repositories.RouteFareRepository;
import com.astrotech.transport.repositories.RouteRepository;
import com.astrotech.transport.validators.role.RoleRequired;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RouteFareService {
    private final RouteFareRepository routeFareRepository;
    private final RouteRepository routeRepository;

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @Caching(evict = {
            @CacheEvict(value = "fare-lists", allEntries = true),
            @CacheEvict(value = "route-with-fares", allEntries = true)
    })
    public RouteFareResponse createRouteFare(RouteFareRequest request, UUID routeId) {
        Objects.requireNonNull(routeId, "Route ID cannot be null");
        Objects.requireNonNull(request, "Request cannot be null");
        var route = routeRepository.findById(routeId).orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        validateRequest(request);
        validateVehicleClassNotAlreadyConfigured(routeId, request.vehicleClass());
        var routeFare = RouteFareMapper.createRouteFare(request, route);
        var savedRouteFare = routeFareRepository.save(routeFare);
        return RouteFareMapper.routeFareResponse(savedRouteFare);


    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    @Caching(evict = {
            @CacheEvict(value = "route-fares", key = "#routeFareId"),
            @CacheEvict(value = "fare-lists", allEntries = true),
            @CacheEvict(value = "route-with-fares", allEntries = true)
    })
    public SimpleRouteFareResponse updateRouteFare(
            RouteFareUpdateRequest request,
            UUID routeFareId
    ) {
        Objects.requireNonNull(request, "Route fare update request cannot be null");
        Objects.requireNonNull(routeFareId, "Route fare ID cannot be null");

        var routeFare = routeFareRepository.findById(routeFareId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route fare not found for the given ID"
                        )
                );

        var finalAmount = request.amount() != null
                ? request.amount()
                : routeFare.getAmount();

        var finalVehicleClass = request.vehicleClass() != null
                ? request.vehicleClass()
                : routeFare.getVehicleClass();

        var finalEffectiveFrom = request.effectiveFrom() != null
                ? request.effectiveFrom()
                : routeFare.getEffectiveFrom();

        var finalEffectiveTo = request.effectiveTo() != null
                ? request.effectiveTo()
                : routeFare.getEffectiveTo();

        validateAmount(finalAmount);

        validateEffectiveDates(
                finalEffectiveFrom,
                finalEffectiveTo
        );

        if (request.vehicleClass() != null
                && request.vehicleClass() != routeFare.getVehicleClass()) {

            validateVehicleClassNotAlreadyUsed(
                    routeFare.getRoute().getId(),
                    finalVehicleClass,
                    routeFareId
            );
        }

        routeFare.setAmount(finalAmount);
        routeFare.setVehicleClass(finalVehicleClass);
        routeFare.setEffectiveFrom(finalEffectiveFrom);
        routeFare.setEffectiveTo(finalEffectiveTo);

        if (request.active() != null) {
            routeFare.setActive(request.active());
        }

        var savedRouteFare = routeFareRepository.save(routeFare);

        return RouteFareMapper.simpleRouteFareResponse(savedRouteFare);
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "route-fares",
            key = "#routeFareId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public RouteFareResponse getRouteFare(UUID routeFareId) {
        return routeFareRepository.findById(routeFareId)
                .map(RouteFareMapper::routeFareResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Route fare not found"));
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "route-with-fares",
            key = "'single-' + #routeId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public RouteFareResponse getRouteWithFare(UUID routeId) {
        return routeFareRepository.findByRouteId(routeId)
                .map(RouteFareMapper::routeFareResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Route fare not found"));
    }

    @Transactional(readOnly = true)

    @CustomCacheable(
            value = "route-with-fares",
            key = "'list-' + #routeId + '-p' + #page + '-s' + #size",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<RouteFareResponse> getRouteWithFares(UUID routeId, int page, int size) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, "destinationCity", true, RouteFare.class, true);
        var result = routeFareRepository.findAllByRouteId(routeId, pageable);

        var content = result
                .getContent()
                .stream()
                .map(RouteFareMapper::routeFareResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN, UserRole.DRIVER, UserRole.TERMINAL_SUPERVISOR})
    @CustomCacheable(
            value = "fare-lists",
            key = "'all-p' + #page + '-s' + #size",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleRouteFareResponse> getAllFares(int page, int size) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, "destinationCity", true, RouteFare.class, true);
        var result = routeFareRepository.findAllBy(pageable);

        var content = result
                .getContent()
                .stream()
                .map(RouteFareMapper::simpleRouteFareResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    private void validateVehicleClassNotAlreadyConfigured(
            UUID routeId,
            VehicleClass vehicleClass
    ) {
        if (routeFareRepository.existsByRouteIdAndVehicleClass(
                routeId,
                vehicleClass
        )) {
            throw new ConflictException(
                    "A fare for this vehicle class already exists on this route."
            );
        }
    }

    private void validateRequest(RouteFareRequest request) {

        if (request.vehicleClass() == null) {
            throw new ValidationException(
                    "Vehicle class is required."
            );
        }

        if (request.amount() == null) {
            throw new ValidationException(
                    "Fare amount is required."
            );
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "Fare amount must be greater than zero."
            );
        }

        if (request.effectiveFrom() == null) {
            throw new ValidationException(
                    "Effective-from date is required."
            );
        }

        if (request.effectiveTo() == null) {
            throw new ValidationException(
                    "Effective-to date is required."
            );
        }

        if (!request.effectiveTo().isAfter(request.effectiveFrom())) {
            throw new ValidationException(
                    "Effective-to date must be after effective-from date."
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException(
                    "Fare amount is required."
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "Fare amount must be greater than zero."
            );
        }
    }

    private void validateEffectiveDates(
            Instant effectiveFrom,
            Instant effectiveTo
    ) {
        if (effectiveFrom == null) {
            throw new ValidationException(
                    "Effective-from date is required."
            );
        }

        if (effectiveTo == null) {
            throw new ValidationException(
                    "Effective-to date is required."
            );
        }

        if (effectiveTo.isAfter(effectiveFrom)) {
            throw new ValidationException(
                    "Effective-to date cannot be before effective-from date."
            );
        }

    }

    private void validateVehicleClassNotAlreadyUsed(
            UUID routeId,
            VehicleClass vehicleClass,
            UUID routeFareId
    ) {
        boolean exists = routeFareRepository
                .existsByRouteIdAndVehicleClassAndIdNot(
                        routeId,
                        vehicleClass,
                        routeFareId
                );

        if (exists) {
            throw new ConflictException(
                    "A route fare for this vehicle class already exists on this route."
            );
        }
    }
}
