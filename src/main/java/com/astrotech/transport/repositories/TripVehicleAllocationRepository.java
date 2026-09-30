package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.TripVehicleAllocation;
import com.astrotech.transport.enums.*;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.*;


@Repository
public interface TripVehicleAllocationRepository
        extends JpaRepository<TripVehicleAllocation, UUID> {

    @Query("""
            SELECT a
            FROM TripVehicleAllocation a
            JOIN FETCH a.vehicle
            LEFT JOIN FETCH a.driverProfile
            WHERE a.trip.id = :tripId
              AND a.role = :role
              AND a.status = :status
            """)
    Optional<TripVehicleAllocation> findByTripAndRoleAndStatus(
            UUID tripId,
            AllocationRole role,
            AllocationStatus status
    );

    @Query("""
            SELECT a
            FROM TripVehicleAllocation a
            JOIN FETCH a.vehicle
            LEFT JOIN FETCH a.driverProfile
            WHERE a.trip.id = :tripId
              AND a.role = :role
              AND a.status IN :statuses
            """)
    List<TripVehicleAllocation> findByTripAndRoleAndStatuses(
            UUID tripId,
            AllocationRole role,
            Collection<AllocationStatus> statuses
    );

    @Query("""
            SELECT a
            FROM TripVehicleAllocation a
            WHERE a.trip.id = :tripId
              AND a.vehicle.id = :vehicleId
              AND a.status IN :statuses
            """)
    Optional<TripVehicleAllocation> findActiveAllocation(
            UUID tripId,
            UUID vehicleId,
            Collection<AllocationStatus> statuses
    );

    @Query("""
            SELECT COUNT(a) > 0
            FROM TripVehicleAllocation a
            WHERE a.vehicle.id = :vehicleId
              AND a.status IN :statuses
            """)
    boolean existsActiveAllocationForVehicle(
            UUID vehicleId,
            Collection<AllocationStatus> statuses
    );

    @Query("""
            SELECT COUNT(a) > 0
            FROM TripVehicleAllocation a
            WHERE a.driverProfile.id = :driverId
              AND a.status IN :statuses
            """)
    boolean existsActiveAllocationForDriver(
            UUID driverId,
            Collection<AllocationStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("""
                SELECT a
                FROM TripVehicleAllocation a
                JOIN FETCH a.vehicle v
                JOIN FETCH a.trip t
                LEFT JOIN FETCH t.driver
                WHERE v.id = :vehicleId
                  AND v.status IN :statuses
                  AND a.role = :role
                  AND a.status = :status
            """)
    Optional<TripVehicleAllocation> findActivePrimaryAllocationForVehicle(
            @Param("vehicleId") UUID vehicleId,
            @Param("statuses") Collection<VehicleStatus> statuses,
            @Param("role") AllocationRole role,
            @Param("status") AllocationStatus status
    );
}