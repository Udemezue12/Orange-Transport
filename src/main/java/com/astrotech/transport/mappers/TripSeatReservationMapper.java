package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.SimpleTripSeatReservationResponse;

import com.astrotech.transport.dto.response.TripSeatReservationWithBookingSessionResponse;
import com.astrotech.transport.dto.response.TripSeatReservationWithoutBookingSessionResponse;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.ReservationStatus;

import java.time.Instant;
import java.util.List;

public class TripSeatReservationMapper {
    public static TripSeatReservation createReservation(Trip trip, Seat seat, BookingSession session, String passengerName){
        return TripSeatReservation.builder()
                .trip(trip)
                .seat(seat)
                .passengerName(passengerName)
                .bookingSession(session)
                .status(ReservationStatus.HELD)
                .reservedAt(Instant.now())
                .build();
    }

    public static SimpleTripSeatReservationResponse simpleSeatReservationResponse(TripSeatReservation seatReservation){

        return new SimpleTripSeatReservationResponse(
                seatReservation.getId(),
                seatReservation.getPassengerName(),
                seatReservation.getStatus(),
                seatReservation.getExpiresAt(),
                seatReservation.getReservedAt(),
                seatReservation.getCheckedInAt(),
                seatReservation.getVersion()
        );


    }
    public static TripSeatReservationWithoutBookingSessionResponse mapFromResponse(TripSeatReservation seatReservation){
        var reservationResponse = simpleSeatReservationResponse(seatReservation);
        var  tripResponse = TripMapper.toResponse(seatReservation.getTrip());
        var seatResponse = SeatMapper.toResponse(seatReservation.getSeat());
        return new TripSeatReservationWithoutBookingSessionResponse(
                reservationResponse,
                tripResponse,
                seatResponse
        );

    }
    public static TripSeatReservationWithBookingSessionResponse mapToResponse(TripSeatReservation seatReservation, List<Seat> seats){
        var reservationResponse = simpleSeatReservationResponse(seatReservation);
        var  tripResponse = TripMapper.toResponse(seatReservation.getTrip());
        var seatResponse = SeatMapper.toResponse(seatReservation.getSeat());
        var bookingResponse = BookingSessionMapper.mapToResponse(seatReservation.getBookingSession(), seats);
        return new TripSeatReservationWithBookingSessionResponse(
                reservationResponse,
                tripResponse,
                seatResponse,
                bookingResponse
        );

    }
}
