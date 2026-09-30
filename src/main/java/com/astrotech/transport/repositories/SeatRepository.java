package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.Seat;
import com.astrotech.transport.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findAllByVehicleId(UUID vehicleId);

    Optional<Seat> findByVehicleIdAndSeatNumber(UUID vehicleId, String seatNumber);

    void deleteAllByVehicleId(UUID vehicleId);
    List<Seat> findAllByIdInAndVehicleId(List<UUID> seatIds, UUID vehicleId);

    List<Seat> findByVehicleIdOrderBySeatNumberAsc(UUID vehicleId);


    long countByIdIn(List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.vehicle.id = :vehicleId AND s.status = :status")
    List<Seat> findAllByIdWithPessimisticLock(
            @Param("seatIds") List<UUID> seatIds,
            @Param("vehicleId") UUID vehicleId,
            @Param("status") SeatStatus status
    );
    @Modifying
    @Query("UPDATE Seat s SET s.status = :status WHERE s.id IN :seatIds")
    void updateStatusesForIds(@Param("seatIds") List<UUID> seatIds, @Param("status") SeatStatus status);
}
