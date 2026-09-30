package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.Vehicle;
import com.astrotech.transport.entities.VehicleImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleImagesRepository extends JpaRepository<VehicleImages, UUID> {

    long countByVehicleId(UUID vehicleId);

    @Query("""
                SELECT vi
                FROM VehicleImages vi
                WHERE vi.vehicle.id = :vehicleId
            
            """)
    Optional<VehicleImages> findByVehicleIdWithDetails(UUID vehicleId);

    boolean existsByVehicleIdAndAssetId(UUID vehicleId, String assetId);


    @Query("""
                SELECT vi
                FROM VehicleImages vi
                WHERE vi.vehicle.id = :vehicleId
                ORDER BY vi.id
            """)
    List<VehicleImages> findByVehicleId(@Param("vehicleId") UUID vehicleId);

    ;

    void deleteByVehicleId(UUID vehicleId);


    Optional<VehicleImages> findByIdAndVehicleId(UUID vehicleId, UUID vehicleImageId);
}
