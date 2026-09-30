package com.astrotech.transport.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TicketPdfResponse(
        UUID Id,
        UUID ticketId,
        String secureUrl,
        String publicId,
        String resourceType,
        Boolean created,
        Instant createdAt
) {
}
