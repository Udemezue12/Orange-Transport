package com.astrotech.transport.dto.response;



public record BookingSessionResponse(
        SimpleBookingSessionWithSeatsResponse bookingSession,
        TripForReservationResponse trip,
        UserResponse passenger)



         {
}
