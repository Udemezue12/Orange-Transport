package com.astrotech.transport.repositories;


import com.astrotech.transport.entities.Ticket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByBookingSessionId(UUID bookingSessionId);

    boolean existsByReservationId(UUID id);

    @Query("""
                SELECT t
                FROM Ticket t
                JOIN FETCH t.bookingSession b
                JOIN FETCH t.reservation r
                JOIN FETCH r.seat s
                JOIN FETCH s.vehicle v
                JOIN FETCH r.trip tr
                JOIN FETCH tr.route route
                JOIN FETCH t.passenger p
                WHERE t.id = :ticketId
            """)
    Optional<Ticket> findTicketForPdf(UUID ticketId);
    boolean existsByPaymentId(UUID paymentId);

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Slice<Ticket> findAllByPassengerId(UUID userId, Pageable pageable);
    Slice<Ticket> findAllBy(Pageable pageable);

    Optional<Ticket> findByVerificationToken(UUID token);
}
