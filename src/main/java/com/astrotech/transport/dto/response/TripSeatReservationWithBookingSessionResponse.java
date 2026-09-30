package com.astrotech.transport.dto.response;

public record TripSeatReservationWithBookingSessionResponse(
        SimpleTripSeatReservationResponse reservations,
        TripForReservationResponse trip,
        SeatResponse seat,
        BookingSessionResponse booking

) {
}
