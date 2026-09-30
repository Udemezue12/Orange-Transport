package com.astrotech.transport.dto.response;

public record TripSeatReservationWithoutBookingSessionResponse(
        SimpleTripSeatReservationResponse reservations,
        TripForReservationResponse trip,
        SeatResponse seat

) {
}
