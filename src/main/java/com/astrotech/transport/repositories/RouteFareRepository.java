package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.RouteFare;
import com.astrotech.transport.enums.VehicleClass;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface RouteFareRepository extends JpaRepository<RouteFare, UUID> {
    Optional<RouteFare> findByRouteId(UUID routeId);
    Slice<RouteFare> findAllBy(Pageable pageable);
    @Query("""
        SELECT rf.amount FROM RouteFare rf
        WHERE rf.route.id = :routeId
          AND rf.vehicleClass = :vehicleClass
          AND rf.active = :active
          AND :now BETWEEN rf.effectiveFrom AND rf.effectiveTo
    """)
    Optional<BigDecimal> findActiveFare(
            @Param("routeId") UUID routeId,
            @Param("vehicleClass") VehicleClass vehicleClass,
            @Param("now") Instant now,
            @Param("active") boolean active
    );
    boolean existsByRouteIdAndVehicleClass(
            UUID routeId,
            VehicleClass vehicleClass
    );

    Slice<RouteFare> findAllByRouteId(UUID routeId, Pageable pageable);

    boolean existsByRouteIdAndVehicleClassAndIdNot(UUID routeId, VehicleClass vehicleClass, UUID routeFareId);
}
