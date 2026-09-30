package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.TicketPdf;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketPdfRepository extends JpaRepository<TicketPdf, UUID> {
    Optional<TicketPdf> findByTicketId(UUID ticketId);
    List<TicketPdf> findByCreatedAtBefore(Instant threshold);

    Slice<TicketPdf> findAllByTicketId(UUID ticketId, Pageable pageable);


}
