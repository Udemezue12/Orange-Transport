package com.astrotech.transport.events;

import java.util.UUID;

public record GenerateTicketEvent(
        UUID paymentId
) {
}
