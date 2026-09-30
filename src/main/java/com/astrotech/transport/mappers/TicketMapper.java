package com.astrotech.transport.mappers;

import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.dto.response.PdfTicketResponse;
import com.astrotech.transport.dto.response.SimpleTicketResponse;
import com.astrotech.transport.dto.response.TicketResponse;
import com.astrotech.transport.entities.BookingSession;
import com.astrotech.transport.entities.Payment;
import com.astrotech.transport.entities.Ticket;
import com.astrotech.transport.entities.TripSeatReservation;
import com.astrotech.transport.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TicketMapper {
    public static Ticket createTicket(String ticketNumber, BookingSession session, TicketStatus status, TripSeatReservation reservation, Instant issuedAt, BigDecimal seatPrice, Payment payment) {
        var trimmedTicketNumber = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(ticketNumber, true);
        return Ticket.builder()
                .ticketNumber(trimmedTicketNumber)
                .checkedIn(false)
                .verificationToken(UUID.randomUUID())
                .bookingSession(session)
                .payment(payment)
                .passenger(session.getPassenger())
                .price(seatPrice)
                .status(status)
                .reservation(reservation)
                .issuedAt(issuedAt)
                .build();
    }

    public static SimpleTicketResponse getSimpleResponse(Ticket ticket) {
        return new SimpleTicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getPrice(),
                ticket.getStatus(),
                ticket.getVerificationToken().toString(),
                ticket.isCheckedIn(),
                ticket.getIssuedAt(),
                ticket.getCheckedInAt()
        );
    }

    public static TicketResponse getTicketResponse(Ticket ticket) {
        var userResponse = UserMapper.response(ticket.getPassenger());
        var bookingResponse = BookingSessionMapper.getBookingSessionWithoutSeatsResponse(ticket.getBookingSession());
        var seatResponse = SeatMapper.toResponse(ticket.getReservation().getSeat());
        var tripResponse = TripMapper.mapToResponse(ticket.getReservation().getTrip());
        var reservationResponse = TripSeatReservationMapper.simpleSeatReservationResponse(ticket.getReservation());
        var ticketResponse = getSimpleResponse(ticket);
        return new TicketResponse(
                ticketResponse,
                bookingResponse,
                tripResponse,
                seatResponse,
                userResponse,
                reservationResponse


        );
    }

    public static PdfTicketResponse toPdfResponse(Ticket ticket) {

        var reservation = ticket.getReservation();
        var seat = reservation.getSeat();
        var trip = reservation.getTrip();
        var vehicle = seat.getVehicle();
        var passenger = ticket.getPassenger();
        var passengerName = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(passenger.getFullName(), true);

        return new PdfTicketResponse(
                ticket.getTicketNumber(),
                passengerName,
                passenger.getEmail(),

                vehicle.getRegistrationNumber(),
                seat.getSeatNumber(),

                trip.getTripCode(),
                trip.getRoute().getOriginTerminal().getName(),
                trip.getRoute().getDestinationTerminal().getName(),
                ticket.getVerificationToken().toString(),
                trip.getScheduledDepartureTime(),
                trip.getScheduledArrivalTime(),
                trip.getBoardingTime(),
                ticket.getPayment().getCurrency().getSymbol(),
                ticket.getPrice(),
                ticket.isCheckedIn(),
                ticket.getIssuedAt(),
                ticket.getCheckedInAt()
        );
    }
}
