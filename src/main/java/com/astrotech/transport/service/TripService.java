package com.astrotech.transport.service;


import com.astrotech.transport.core.*;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.*;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.exceptions.*;
import com.astrotech.transport.mappers.TripMapper;
import com.astrotech.transport.repositories.*;
import com.astrotech.transport.util.CodeGenerator;
import com.astrotech.transport.validators.role.RoleRequired;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CodeGenerator tripCodeGenerator;
    private final TripVehicleAllocationService allocationVehicleService;
    private final ZoneId zoneId;
    private final AssignWorkerService assignWorkerService;
    private final GetCurrentUser getCurrentUser;


    @Transactional
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @CacheEvict(value = "trip-lists", allEntries = true)
    public TripResponse createTrip(TripRequest request, UUID currentUserId) {
        var tripCode = tripCodeGenerator.generateUniqueTripCode();
        var trimmedTripCode = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(tripCode, true);
        if (tripRepository.existsByTripCode(trimmedTripCode)) {
            throw new ConflictException("Trip code already exists: " + trimmedTripCode);
        }

        var createdBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        var route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + request.routeId()));


        if (createdBy.getRole() == UserRole.TERMINAL_SUPERVISOR) {
            var originTerminal = route.getOriginTerminal();

            if (originTerminal.getTerminalSupervisor() == null ||
                    !originTerminal.getTerminalSupervisor().getId().equals(createdBy.getId())) {
                throw new AccessDeniedException("You can only create trips departing from " + originTerminal.getName());
            }
        }

        var vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));
        if (!vehicle.getStatus().canParticipateInTrip()) {
            throw new BadRequestException("Vehicle is not available for assignment. Current status: " + vehicle.getStatus());
        }
        var driver = driverProfileRepository.findById(request.driverProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + request.driverProfileId()));
        if (!driver.isLicenseVerified()) {
            throw new BadRequestException("Driver license is not yet verified: " + request.driverProfileId());
        }

        if (!driver.isActive()) {
            throw new BadRequestException("Driver account is inactive: " + request.driverProfileId());
        }

        var vehicleLoader = profileRepository.findById(request.vehicleLoaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Loader not found"));

        validateVehicleLoader(vehicleLoader);


        var trip = TripMapper.createTrip(request, trimmedTripCode, vehicle, route, createdBy, driver, vehicleLoader);


        var savedTrip = tripRepository.save(trip);

        assignWorkerService.assignWorkers(
                getWorkerAssignmentRequests(vehicleLoader, driver),
                createdBy.getId(),
                savedTrip.getId()
        );
        allocationVehicleService.createAllocationVehicle(savedTrip, savedTrip.getVehicle(), savedTrip.getDriver(), AllocationRole.PRIMARY, AllocationStatus.ACTIVE);
        return TripMapper.mapToTripResponse(savedTrip);


    }




    @Transactional
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @Caching(evict = {
            @CacheEvict(value = "trips", allEntries = true),
            @CacheEvict(value = "trip-lists", allEntries = true)
    })
    public TripResponse updateTrip(UUID tripId, TripUpdateRequest request) {
        var currentUser = getCurrentUser.getCurrentUser();
        var trip = getTrip(tripId);
        if (request.scheduledArrivalTime() != null) {
            trip.setScheduledArrivalTime(request.scheduledArrivalTime());
        }
        if (request.scheduledDepartureTime() != null) {
            trip.setScheduledArrivalTime(request.scheduledDepartureTime());
        }


        if (request.actualDepartureTime() != null) {
            var bookingCutoff = request.actualDepartureTime().minus(30, ChronoUnit.MINUTES);
            trip.setActualDepartureTime(request.actualDepartureTime());
            trip.setBookingCutoff(bookingCutoff);

        }
        if (request.actualArrivalTime() != null) {

            trip.setActualArrivalTime(request.actualArrivalTime());

        }
        if (request.status() != null) {
            trip.setStatus(request.status());
            trip.getVehicle().setStatus(getVehicleStatus(request.status()));
            assignWorkerService.updateAssignStatus(trip.getId(), getAssignStatus(request.status()));
        }

        if (request.boardingTime() != null) {
            trip.setBoardingTime(request.boardingTime());
        }


        if (request.vehicleId() != null) {

            var isNewVehicle = trip.getVehicle() == null || !request.vehicleId().equals(trip.getVehicle().getId());

            if (isNewVehicle) {
                var vehicle = vehicleRepository.findById(request.vehicleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));

                if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
                    throw new BadRequestException("Vehicle is not available for assignment. Current status: " + vehicle.getStatus());
                }

                trip.setVehicle(vehicle);
            }
        }

        if (request.routeId() != null) {
            var isNewRoute = trip.getRoute() == null || !request.routeId().equals(trip.getRoute().getId());
            if (isNewRoute) {
                var route = routeRepository.findById(request.routeId())
                        .orElseThrow(() -> new EntityNotFoundException("Route not found: " + request.routeId()));
                trip.setRoute(route);
            }
        }

        if (request.driverProfileId() != null) {
            var isNewDriver = trip.getDriver() == null || !request.driverProfileId().equals(trip.getDriver().getId());
            if (isNewDriver) {
                var driver = driverProfileRepository.findByIdAndLicenseVerifiedTrueAndActiveTrue(request.driverProfileId())
                        .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + request.driverProfileId()));
                trip.setDriver(driver);
                assignWorkerService.updateAssignedWorker(trip.getId(), driver.getUser(), currentUser, UserRole.DRIVER, AssignedServiceType.TRIP_VEHICLE_DRIVER);
            }

        }


        if (request.vehicleLoaderId() != null) {
            var isNewLoader = trip.getVehicleLoaderProfile() == null
                    || !request.vehicleLoaderId()
                    .equals(trip.getVehicleLoaderProfile().getId());
            if (isNewLoader) {
                var vehicleLoader = profileRepository.findById(request.vehicleLoaderId())
                        .orElseThrow(() -> new EntityNotFoundException("VehicleLoader not found: " + request.driverProfileId()));
                validateVehicleLoader(vehicleLoader);
                trip.setVehicleLoaderProfile(vehicleLoader);
                assignWorkerService.updateAssignedWorker(trip.getId(), vehicleLoader.getUser(), currentUser, UserRole.VEHICLE_LOADER, AssignedServiceType.TRIP_VEHICLE_LOADER);
            }

        }


        return TripMapper.mapToTripResponse(trip);
    }

    @Transactional
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @Caching(evict = {
            @CacheEvict(value = "trips", allEntries = true),
            @CacheEvict(value = "trip-lists", allEntries = true)
    })
    public SimpleTripResponse updateTripStatus(UUID tripId, UpdateTripStatus request) {
        var trip = getTrip(tripId);
        trip.setStatus(request.status());
        trip.getVehicle().setStatus(getVehicleStatus(request.status()));
        assignWorkerService.updateAssignStatus(trip.getId(), getAssignStatus(request.status()));

        var savedTrip = tripRepository.save(trip);
        return TripMapper.mapToSimpleResponse(savedTrip);


    }

    @Transactional
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @Caching(evict = {
            @CacheEvict(value = "trips", allEntries = true),
            @CacheEvict(value = "trip-lists", allEntries = true)
    })
    public SimpleTripResponse updateTripStatusWithReason(UUID tripId, UpdateTripDelayWithReason request) {
        var trip = getTrip(tripId);


        if (isFinalState(trip.getStatus())) {
            return TripMapper.mapToSimpleResponse(trip);
        }

        var newStatus = request.status();


        if (newStatus == null || isFinalState(newStatus)) {
            throw new BadRequestException("Target status is missing or not allowed.");
        }


        switch (newStatus) {
            case DELAYED -> {
                trip.setStatus(TripStatus.DELAYED);
                trip.setDelayReason(request.delayReason());
            }
            case CANCELLED -> trip.setStatus(TripStatus.CANCELLED);
            default -> throw new BadRequestException("Unsupported trip status transition: " + newStatus);
        }

        var savedTrip = tripRepository.save(trip);
        return TripMapper.mapToSimpleResponse(savedTrip);
    }


    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "trips",
            key = "'code-' + #tripCode",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public TripCodeResponse getByTripCode(String tripCode) {
        var trimmedTripCode = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(tripCode, true);
        return tripRepository.findByTripCode(trimmedTripCode)
                .map(TripMapper::mapToTripCodeResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found or does not exist: " + trimmedTripCode));

    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.TERMINAL_SUPERVISOR, UserRole.ADMIN})
    @CustomCacheable(
            value = "trips",
            key = "'user-' + #userId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public TerminalSupervisorTripResponse getTripByUser(UUID userId) {
        return tripRepository.findByCreatedById(userId)
                .map(TripMapper::mapToSupervisorResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found for: " + userId));

    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.TERMINAL_SUPERVISOR, UserRole.ADMIN, UserRole.DRIVER})
    @CustomCacheable(
            value = "trips",
            key = "'vehicle-' + #vehicleId",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public VehicleTripResponse getTripByVehicleId(UUID vehicleId) {
        return tripRepository.findByVehicleId(vehicleId)
                .map(TripMapper::mapToVehicleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found for: " + vehicleId));

    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.TERMINAL_SUPERVISOR, UserRole.ADMIN, UserRole.DRIVER})
    @CustomCacheable(
            value = "trips",
            key = "'route-' + #routeId",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public RouteTripResponse getTripByRouteId(UUID routeId) {
        return tripRepository.findByRouteId(routeId)
                .map(TripMapper::mapToRouteResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found for: " + routeId));

    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.TERMINAL_SUPERVISOR, UserRole.ADMIN, UserRole.DRIVER})
    @CustomCacheable(
            value = "trips",
            key = "'driver-' + #driverId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public DriverTripResponse getTripByDriverId(UUID driverId) {
        return tripRepository.findByDriverId(driverId)
                .map(TripMapper::mapToDriverResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found for: " + driverId));

    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @CustomCacheable(
            value = "trip-lists",
            key = "'all-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTripResponse> getAllTrips(int page, int size, String sortBy) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Trip.class, true);
        var result = tripRepository.findAllBy(pageable);
        return getSimpleTripResponseSliceResponse(result);
    }


    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN, UserRole.DRIVER, UserRole.TERMINAL_SUPERVISOR})
    @CustomCacheable(
            value = "trip-lists",
            key = "'vehicle-' + #vehicleId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTripResponse> getAllTripsByVehicleId(UUID vehicleId, int page, int size, String sortBy) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found: " + vehicleId);
        }
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Trip.class, true);
        var result = tripRepository.findAllByVehicleId(vehicleId, pageable);
        return getSimpleTripResponseSliceResponse(result);
    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @CustomCacheable(
            value = "trip-lists",
            key = "'route-' + #routeId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTripResponse> getAllTripsByRouteId(UUID routeId, int page, int size, String sortBy) {
        if (!routeRepository.existsById(routeId)) {
            throw new ResourceNotFoundException("Route not found: " + routeId);
        }
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Trip.class, true);
        var result = tripRepository.findAllByRouteId(routeId, pageable);
        return getSimpleTripResponseSliceResponse(result);
    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR, UserRole.DRIVER})
    @CustomCacheable(
            value = "trip-lists",
            key = "'driver-' + #driverId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTripResponse> getAllTripsByDriverId(UUID driverId, int page, int size, String sortBy) {
        if (!driverProfileRepository.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver Profile not found: " + driverId);
        }
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Trip.class, true);
        var result = tripRepository.findAllByDriverId(driverId, pageable);
        return getSimpleTripResponseSliceResponse(result);
    }

    @Transactional
    @CustomCacheable(
            value = "trip-lists",
            key = "'search-' + #searchDate + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 30,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTripResponse> searchTrips(TripSearchRequest request, LocalDate searchDate, int page, int size) {
        var originTerminalId = request.originTerminalId();
        var destinationTerminalId = request.destinationTerminalId();

        if (originTerminalId.equals(destinationTerminalId)) {
            throw new BadRequestException("Origin and Destination terminals cannot be the same.");
        }

        var pageable = GetPageRequest.getPageableWithSorting(page, size, "scheduledDepartureTime", false, Trip.class, false);

        var startOfDay = searchDate
                .atStartOfDay(zoneId)
                .toInstant();

        var endOfDay = searchDate
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toInstant();

        var now = Instant.now();

        var result = tripRepository.findAvailableTrips(
                originTerminalId,
                destinationTerminalId,
                startOfDay,
                endOfDay,
                now,
                TripStatus.SCHEDULED,
                pageable
        );

        var content = result.getContent()
                .stream()
                .map(TripMapper::mapToSimpleResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()


        );

    }

    private boolean isFinalState(TripStatus status) {
        return status == TripStatus.BOARDING
                || status == TripStatus.DEPARTED
                || status == TripStatus.ARRIVED
                || status == TripStatus.COMPLETED
                ;
    }

    private VehicleStatus getVehicleStatus(TripStatus status) {
        return switch (status) {
            case TRANSSHIPMENT -> null;
            case COMPLETED, CANCELLED -> VehicleStatus.AVAILABLE;
            case SCHEDULED -> VehicleStatus.ASSIGNED;
            case BOARDING -> VehicleStatus.BOARDING;
            case DEPARTED -> VehicleStatus.ON_TRIP;
            case ARRIVED -> VehicleStatus.UNLOADING;
            case DELAYED -> VehicleStatus.ON_HOLD;
        };
    }

    private AssignStatus getAssignStatus(TripStatus status) {
        return switch (status) {
            case COMPLETED -> AssignStatus.COMPLETED;
            case DEPARTED, BOARDING, SCHEDULED -> AssignStatus.ACTIVE;
            case CANCELLED -> AssignStatus.CANCELLED;
            case TRANSSHIPMENT, ARRIVED, DELAYED -> null;
        };
    }


    public Trip getTrip(UUID tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + tripId));
    }

    public Trip getLockedTrip(UUID tripId) {
        return tripRepository.findByIdForUpdate(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + tripId));
    }

    private static @NonNull SliceResponse<SimpleTripResponse> getSimpleTripResponseSliceResponse(Slice<Trip> result) {
        var content = result.getContent()
                .stream()
                .map(TripMapper::mapToSimpleResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()


        );
    }

    private static @NonNull List<WorkerAssignmentRequest> getWorkerAssignmentRequests(Profile vehicleLoader, DriverProfile driver) {
        return List.of(
                new WorkerAssignmentRequest(
                        vehicleLoader.getUser().getId(),
                        AssignedServiceType.TRIP_VEHICLE_LOADER
                ),
                new WorkerAssignmentRequest(
                        driver.getUser().getId(),
                        AssignedServiceType.TRIP_VEHICLE_DRIVER
                )
        );
    }
    private static void validateVehicleLoader(Profile vehicleLoaderProfile) {

        if (vehicleLoaderProfile.getUser().getRole() != UserRole.VEHICLE_LOADER) {
            throw new BadRequestException("This user does not have the vehicle loader role.");
        }


        if (vehicleLoaderProfile.getIdentityDocument().getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new BadRequestException("This user's identity document has not been verified by management.");
        }
    }


}
