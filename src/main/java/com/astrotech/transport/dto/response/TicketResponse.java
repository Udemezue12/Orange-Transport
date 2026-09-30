package com.astrotech.transport.dto.response;

public record TicketResponse(
        SimpleTicketResponse ticket,
        SimpleBookingSessionWithoutSeatsResponse booking,
        TripWithoutUserResponse trip,
        SeatResponse vehicle,
        UserResponse user,
        SimpleTripSeatReservationResponse reservation

) {
}

