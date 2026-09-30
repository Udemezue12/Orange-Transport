package com.astrotech.transport.mappers;

import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.dto.response.SimpleBookingSessionResponse;
import com.astrotech.transport.dto.response.BookingSessionResponse;
import com.astrotech.transport.dto.response.SimpleBookingSessionWithSeatsResponse;
import com.astrotech.transport.dto.response.SimpleBookingSessionWithoutSeatsResponse;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.BookingSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class BookingSessionMapper {
    public static BookingSession createBooking(String reference, Trip trip, User passenger, BigDecimal totalAmount, Instant expiresAt, Instant now) {
        var trimmedReference = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(reference, true);
        return BookingSession.builder()
                .reference(trimmedReference)
                .trip(trip)
                .passenger(passenger)
                .status(BookingSessionStatus.PENDING)
                .totalAmount(totalAmount)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();
    }
    public static BookingSessionResponse mapToResponse(BookingSession session, List<Seat> seats) {

//        var seats = session.getReservations()
//                .stream()
//                .map(TripSeatReservation::getSeat)
//                .toList();
        var trip = TripMapper.toResponse(session.getTrip());
        var passenger = UserMapper.response(session.getPassenger());
        var bookingSession = getBookingSessionWithSeatsResponse(session, seats);


        return new BookingSessionResponse(
                bookingSession,
                trip,
                passenger
        );
    }

    public static SimpleBookingSessionResponse getBookingSessionResponse(BookingSession session, List<UUID> seatIds) {

        return new SimpleBookingSessionResponse(
                session.getId(),
                session.getReference(),
                session.getStatus(),
                session.getTotalAmount(),
                session.getExpiresAt(),
                session.getCreatedAt(),
                seatIds
        );
    }
    public static SimpleBookingSessionWithSeatsResponse getBookingSessionWithSeatsResponse(BookingSession session, List<Seat> seats) {
        var results = SeatMapper.toListResponse(seats);

        return new SimpleBookingSessionWithSeatsResponse(
                session.getId(),
                session.getReference(),
                session.getStatus(),
                session.getTotalAmount(),
                session.getExpiresAt(),
                session.getCreatedAt(),
                results
        );
    }
    public static SimpleBookingSessionWithoutSeatsResponse getBookingSessionWithoutSeatsResponse(BookingSession session) {


        return new SimpleBookingSessionWithoutSeatsResponse(
                session.getId(),
                session.getReference(),
                session.getStatus(),
                session.getTotalAmount(),
                session.getExpiresAt(),
                session.getCreatedAt()
        );
    }



}
