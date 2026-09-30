package com.astrotech.transport.fintech.foreignGateway;

import com.astrotech.transport.dto.request.WebhookRequest;
import com.astrotech.transport.dto.response.WebhookPaymentStatus;
import com.astrotech.transport.entities.BookingSession;

import java.util.Optional;

public interface ForeignPaymentGateway {
    CheckOutSession createCheckSession(BookingSession bookingSession);

    Optional<WebhookPaymentStatus> parseWebhookRequest(WebhookRequest request);
}
