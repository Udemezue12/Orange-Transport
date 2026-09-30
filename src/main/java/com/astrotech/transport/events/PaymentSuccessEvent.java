package com.astrotech.transport.events;

import java.util.UUID;

public record PaymentSuccessEvent(
        String bookingCode,
        String name,
        String email,
        UUID paymentId,
        String phoneNumber
) {
}