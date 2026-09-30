package com.astrotech.transport.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PdfTicketResponse(
        String ticketNumber,
        String passengerName,
        String passengerEmail,

        String vehicleRegistrationNumber,
        String seatNumber,

        String tripCode,
        String originTerminal,
        String DestinationTerminal,
        String verificationToken,

        Instant scheduledDepartureTime,
        Instant scheduledArrivalTime,
        Instant boardingTime,
        String currency,

        BigDecimal price,
        Boolean checkedIn,
        Instant issuedAt,
        Instant checkedInAt
) {
}
