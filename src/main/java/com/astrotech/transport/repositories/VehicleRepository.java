package com.astrotech.transport.repositories;


import com.astrotech.transport.entities.Vehicle;
import com.astrotech.transport.enums.VehicleClass;
import com.astrotech.transport.enums.VehicleStatus;
import com.astrotech.transport.projection.ExistingVehicleFields;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    @Query("""
            SELECT
                v.registrationNumber AS registrationNumber,
                v.chassisNumber AS chassisNumber,
                v.engineNumber AS engineNumber,
                v.vin as vin
            FROM Vehicle v
            WHERE
                  v.chassisNumber = :chassisNumber
               OR v.engineNumber = :engineNumber
               OR v.vin = :vin
            """)
    List<ExistingVehicleFields> findExistingFields(

            String chassisNumber,
            String engineNumber,
            String vin
    );

    @Query("""
                SELECT DISTINCT v
                FROM Vehicle v
                LEFT JOIN FETCH v.seats
                WHERE v.id = :id
            """)
    Optional<Vehicle> findByIdWithSeats(@Param("id") UUID id);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("""
            SELECT v
            FROM Vehicle v
            WHERE v.id = :vehicleId
                        AND v.status in :statuses
            """)
    Optional<Vehicle> findByIdForUpdate(@Param("vehicleId")UUID vehicleId,@Param("statuses") Collection<VehicleStatus> statuses);

    boolean existsByChassisNumber(String chassisNumber);

    boolean existsByEngineNumber(String engineNumber);

    boolean existsByVin(String vinNumber);

    Optional<Vehicle> findTopByVehicleClassOrderByRegistrationNumberDesc(VehicleClass vehicleClass);
}
