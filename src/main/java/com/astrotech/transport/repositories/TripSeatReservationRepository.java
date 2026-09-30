package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.TripSeatReservation;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface TripSeatReservationRepository extends JpaRepository<TripSeatReservation, UUID> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("""
                SELECT COUNT(r) > 0
                FROM TripSeatReservation r
                WHERE r.trip.id = :tripId
                  AND r.seat.id IN :seatIds
                  AND r.status IN ('HELD', 'BOOKED', 'CONFIRMED', 'CHECKED_IN')
                  AND (r.expiresAt IS NULL OR r.expiresAt > :now)
            """)
    boolean existsActiveReservationsForTripAndSeats(
            @Param("tripId") UUID tripId,
            @Param("seatIds") List<UUID> seatIds,
            @Param("now") Instant now
    );

    @Query("""
                SELECT r
                FROM TripSeatReservation r
                JOIN FETCH r.seat
                WHERE r.bookingSession.id = :sessionId
                ORDER BY r.seat.rowNumber ASC, r.seat.seatNumber ASC
            """)
    List<TripSeatReservation> findByBookingSessionId(@Param("sessionId") UUID sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("""
            SELECT r
            FROM TripSeatReservation r
            JOIN FETCH r.seat
            WHERE r.bookingSession.id = :bookingSessionId
            """)
    List<TripSeatReservation> findByBookingSessionIdForUpdate(
            @Param("bookingSessionId") UUID bookingSessionId
    );

    @Query("""
                SELECT DISTINCT tsr
                FROM TripSeatReservation tsr
                LEFT JOIN FETCH tsr.trip trp
                LEFT JOIN FETCH tsr.seat
                LEFT JOIN FETCH trp.seatReservations siblingRs
                LEFT JOIN FETCH siblingRs.seat
                WHERE tsr.id = :reservationId
            """)
    Optional<TripSeatReservation> findByIdWithFullSessionDetails(
            @Param("reservationId") UUID reservationId
    );

    Slice<TripSeatReservation> findAllBy(Pageable pageable);

    Slice<TripSeatReservation> findAllByBookingSessionId(UUID bookingId, Pageable pageable);


}
