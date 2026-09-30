package com.astrotech.transport.utilities.pdf;

import com.astrotech.transport.entities.User;

import java.util.UUID;

public record GeneratedPdf(
        byte[] bytes,
        String filename,
        UUID ticketId,
        String ticketNumber,
        User user
) {}
