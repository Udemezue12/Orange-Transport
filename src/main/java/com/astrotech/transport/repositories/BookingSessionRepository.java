package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.BookingSession;
import com.astrotech.transport.enums.BookingSessionStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingSessionRepository extends JpaRepository<BookingSession, UUID> {
    @EntityGraph(attributePaths = {
            "reservations",
            "reservations.seat"
    })
    @NonNull
    Optional<BookingSession> findById(@NonNull UUID id);

    @Query("""
                SELECT s FROM BookingSession s
                JOIN FETCH s.trip
                JOIN FETCH s.passenger
                WHERE s.reference = :reference
            """)
    Optional<BookingSession> findByReferenceWithDetails(@Param("reference") String reference);


    @Query("""
            SELECT s
            FROM BookingSession s
            JOIN FETCH s.trip
            JOIN FETCH s.passenger
            WHERE s.passenger.id = :passengerId
            ORDER BY s.createdAt DESC
            """)
    Slice<BookingSession> findByPassengerIdWithDetails(
            @Param("passengerId") UUID passengerId,
            Pageable pageable
    );

    @Query("""
            SELECT s
            FROM BookingSession s
            JOIN FETCH s.trip
            JOIN FETCH s.passenger
            WHERE s.reference = :sessionReference
              AND s.passenger.id = :passengerId
            """)
    Optional<BookingSession> findBySessionReferenceAndPassengerIdWithDetails(
            @Param("sessionReference") String sessionReference,
            @Param("passengerId") UUID passengerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("""
            SELECT DISTINCT b
            FROM BookingSession b
            JOIN FETCH b.reservations r
            JOIN FETCH r.seat
            JOIN FETCH b.trip
            JOIN FETCH b.passenger
            WHERE b.id = :bookingId
            """)
    Optional<BookingSession> findByIdForUpdate(
            @Param("bookingId") UUID bookingId
    );

    @Query("""
            SELECT s
            FROM BookingSession s
            JOIN FETCH s.trip
            JOIN FETCH s.passenger
            ORDER BY s.createdAt DESC
            """)
    Slice<BookingSession> findAllWithDetails(Pageable pageable);

    Optional<BookingSession> findByPassengerId(UUID passengerId
    );

    Optional<BookingSession> findByTripId(UUID tripId
    );

    Optional<BookingSession> findByReference(String reference);

    @Modifying
    @Query("UPDATE BookingSession b SET b.status = :targetStatus " +
            "WHERE b.status = :currentStatus AND b.createdAt <= :cutoffTime")
    int updateStatusByCurrentStatusAndCreatedAtBefore(
            @Param("currentStatus") BookingSessionStatus currentStatus,
            @Param("targetStatus") BookingSessionStatus targetStatus,
            @Param("cutoffTime") Instant cutoffTime
    );

    @Query("SELECT b FROM BookingSession b WHERE b.status = :status AND b.createdAt <= :cutoffTime")
    List<BookingSession> findAllByStatusAndCreatedAtBefore(
            @Param("status") BookingSessionStatus status,
            @Param("cutoffTime") Instant cutoffTime
    );
}
