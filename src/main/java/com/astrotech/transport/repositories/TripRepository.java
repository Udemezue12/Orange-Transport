package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.Trip;
import com.astrotech.transport.enums.TripStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {
    Slice<Trip> findAllBy(Pageable pageable);
    boolean existsByTripCode(String tripCode);

    Optional<Trip> findByCreatedById(UUID createdById);

    Optional<Trip> findByRouteId(UUID routeId);

    Optional<Trip> findByDriverId(UUID driverId);

    Optional<Trip> findByVehicleId(UUID vehicleId);

    Slice<Trip> findAllByVehicleId(UUID vehicleId, Pageable pageable);

    Slice<Trip> findAllByRouteId(UUID routeId, Pageable pageable);

    Slice<Trip> findAllByDriverId(UUID driverProfileId, Pageable pageable);

    Optional<Trip> findByTripCode(String tripCode);

    @Query("""
                SELECT t
                FROM Trip t
                WHERE t.route.originTerminal.id = :originTerminalId
                  AND t.route.destinationTerminal.id = :destinationTerminalId
                  AND t.scheduledDepartureTime >= :startOfDay
                  AND t.scheduledDepartureTime < :endOfDay
                  AND t.bookingCutoff > :now
                  AND t.status = :status
                ORDER BY t.scheduledDepartureTime ASC
            """)
    Slice<Trip> findAvailableTrips(
            @Param("originTerminalId") UUID originTerminalId,
            @Param("destinationTerminalId") UUID destinationTerminalId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay,
            @Param("now") Instant now,
            @Param("status") TripStatus status,
            Pageable pageable
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    Optional<Trip> findByIdForUpdate(@Param("id") UUID id);
}
