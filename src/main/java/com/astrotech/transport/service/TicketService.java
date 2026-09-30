package com.astrotech.transport.service;


import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.BookingSession;
import com.astrotech.transport.entities.Payment;
import com.astrotech.transport.entities.Ticket;
import com.astrotech.transport.entities.TripSeatReservation;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.TicketMapper;
import com.astrotech.transport.repositories.TicketRepository;
import com.astrotech.transport.repositories.TripSeatReservationRepository;
import com.astrotech.transport.util.CodeGenerator;
import com.astrotech.transport.utilities.pdf.GeneratedPdf;
import com.astrotech.transport.utilities.pdf.PdfGenerator;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final TicketRepository ticketRepository;
    private final PdfGenerator pdfGenerator;
    private final TripSeatReservationService reservationService;
    private final BookingSessionService bookingSessionService;
    private final TripSeatReservationRepository seatReservationRepository;
    private final CodeGenerator codeGenerator;
    private final PaymentService paymentService;


    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "tickets",
            key = "'num-' + #ticketNumber",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public TicketResponse getByTicketNumber(String ticketNumber) {
        var trimmedTicketNumber = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(ticketNumber, true);
        return ticketRepository.findByTicketNumber(trimmedTicketNumber)
                .map(TicketMapper::getTicketResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

    }

    @Transactional
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @CacheEvict(value = "passenger-tickets", allEntries = true)
    public List<TicketResponse> generateTicket(UUID paymentId) {
        var payment = paymentService.findById(paymentId);
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment was not successful");

        }
        if (ticketRepository.existsByPaymentId(payment.getId())) {
            throw new BadRequestException("Ticket already exists");
        }
        var session = bookingSessionService.getlockedBookingSession(payment.getBookingSession().getId());
        if (session.getStatus() == BookingSessionStatus.COMPLETED) {
            return ticketRepository
                    .findByBookingSessionId(session.getId())
                    .stream()
                    .map(TicketMapper::getTicketResponse)
                    .toList();
        }

        var tickets = getTickets(session, payment);


        return tickets
                .stream()
                .map(TicketMapper::getTicketResponse)
                .toList();

    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "tickets",
            key = "'token-' + #token",
            ttl = 30,
            timeUnit = TimeUnit.SECONDS
    )
    public PdfTicketResponse verifyTicket(UUID token) {

        var ticket = ticketRepository
                .findByVerificationToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found"
                        ));


        return TicketMapper.toPdfResponse(ticket);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tickets", key = "'num-' + #ticketNumber"),
            @CacheEvict(value = "passenger-tickets", allEntries = true),
            @CacheEvict(value = "ticket-pdfs", allEntries = true)
    })
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    public SimpleTicketResponse checkIn(String ticketNumber) {

        var ticket = ticketRepository
                .findByTicketNumber(ticketNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found"
                        ));

        if (ticket.getStatus() != TicketStatus.ISSUED) {
            throw new BadRequestException(
                    "Ticket is not valid for check-in"
            );
        }

        if (ticket.isCheckedIn()) {
            throw new BadRequestException(
                    "Ticket has already been checked in"
            );
        }

        var reservation = ticket.getReservation();

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Seat reservation is not valid for check-in"
            );
        }

        var now = Instant.now();


        ticket.setCheckedInAt(now);
        ticket.setCheckedIn(true);
        ticket.setStatus(TicketStatus.CHECKED_IN);
        ticket.getBookingSession().getTrip().setStatus(TripStatus.BOARDING);


        reservationService.updateReservationStatus(
                reservation,
                ReservationStatus.CHECKED_IN
        );

        return TicketMapper.getSimpleResponse(ticket);
    }


    @Transactional
    @CacheEvict(value = "passenger-tickets", allEntries = true)
    public List<Ticket> generateTicketWorker(UUID paymentId) {
        var payment = paymentService.findById(paymentId);
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment was not successful");
        }
        var session = bookingSessionService.getlockedBookingSession(payment.getBookingSession().getId());
        if (session.getStatus() == BookingSessionStatus.COMPLETED) {
            return ticketRepository.findByBookingSessionId(session.getId());
        }
        return getTickets(session, payment);
    }


    @Transactional(readOnly = true)
    public GeneratedPdf generateAndConvertTicketPdf(UUID ticketId) {

        log.info("Generating PDF for ticket ID: {}", ticketId);

        var ticket = ticketRepository.findTicketForPdf(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        var response = TicketMapper.toPdfResponse(ticket);

        byte[] bytes = pdfGenerator.generateTicketPdf(response);

        String fileName = "Ticket-" + response.ticketNumber() + ".pdf";
        var customer = ticket.getPayment().getPayer();

        return new GeneratedPdf(
                bytes,
                fileName,
                ticket.getId(),
                ticket.getTicketNumber(),
                customer
        );
    }

    @Transactional
    @RoleRequired({UserRole.PASSENGER, UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @CustomCacheable(
            value = "passenger-tickets",
            key = "#userId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 120,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<SimpleTicketResponse> getAllTicketsByPassenger(UUID userId, int page, int size, String sortBy) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Ticket.class, true);
        var result = ticketRepository.findAllByPassengerId(userId, pageable);
        var content = result.getContent()
                .stream()
                .map(TicketMapper::getSimpleResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional
    @RoleRequired({UserRole.ADMIN})
    public SliceResponse<SimpleTicketResponse> getAllTickets(int page, int size, String sortBy) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Ticket.class, true);
        var result = ticketRepository.findAllBy(pageable);
        var content = result.getContent()
                .stream()
                .map(TicketMapper::getSimpleResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }


    private @NonNull List<Ticket> getTickets(BookingSession session, Payment payment) {
        if (session.getStatus() == BookingSessionStatus.EXPIRED) {
            throw new BadRequestException(
                    "Booking session expired. Payment requires refund."
            );
        }

        if (session.getStatus() != BookingSessionStatus.PAYMENT_PROCESSED) {
            throw new IllegalStateException(
                    "Payment has not been processed yet, wait a little more "
                            + session.getStatus()
            );
        }
        var reservations = seatReservationRepository.findByBookingSessionIdForUpdate(
                session.getId()
        );

        if (reservations.isEmpty()) {
            throw new IllegalStateException(
                    "Booking session contains no reservations"
            );
        }

        var issuedAt = Instant.now();
        var totalSeats = reservations.size();
        if (totalSeats == 0) {
            throw new IllegalStateException("Booking session has no reservations");
        }

        var seatPrice = session.getTotalAmount()
                .divide(
                        BigDecimal.valueOf(totalSeats),
                        2,
                        RoundingMode.HALF_UP
                );

        List<Ticket> tickets = new ArrayList<>(totalSeats);

        for (TripSeatReservation reservation : reservations) {


            if (ticketRepository.existsByReservationId(
                    reservation.getId())) {
                continue;
            }

            if (reservation.getStatus() != ReservationStatus.BOOKED) {
                throw new IllegalStateException(
                        "Reservation " + reservation.getId()
                                + " is not in BOOKED state"
                );
            }
            var ticketNumber = codeGenerator.generateCode("TCK");

            var ticket = TicketMapper.createTicket(ticketNumber, session, TicketStatus.ISSUED, reservation, issuedAt, seatPrice, payment);

            tickets.add(ticket);
            reservation.setStatus(ReservationStatus.CONFIRMED);
        }
        session.setStatus(BookingSessionStatus.COMPLETED);
        return ticketRepository.saveAll(tickets);
    }
}
