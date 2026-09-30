package com.astrotech.transport.exceptions;

import lombok.Getter;


import java.util.UUID;


@Getter
public class TicketGenerationPendingException extends RuntimeException {
    private final UUID ticketId;

    public TicketGenerationPendingException(UUID ticketId) {
        super("Ticket PDF generation started for ID: " + ticketId);
        this.ticketId = ticketId;
    }

}

