package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SimpleTicketResponse(
        UUID id,
        String ticketNumber,
        BigDecimal price,
        TicketStatus status,
        String verificationToken,
        Boolean checkedIn,
        Instant issuedAt,
        Instant checkedInAt


){}
