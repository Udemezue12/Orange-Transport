package com.astrotech.transport.dto.response;



public record PaymentResponse(
        SimplePaymentResponse payment,
        BookingSessionResponse bookedDetails,
        UserResponse user

) {
}
