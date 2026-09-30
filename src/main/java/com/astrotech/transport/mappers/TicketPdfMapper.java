package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.TicketPdfResponse;
import com.astrotech.transport.entities.TicketPdf;
import com.astrotech.transport.enums.TicketPdfStatus;

import java.time.Instant;
import java.util.UUID;

public class TicketPdfMapper {
    public static TicketPdf createPdf(UUID ticketId, String resourceType, String assetId, String secureUrl, String publicId){
        return TicketPdf.builder()
                .ticketId(ticketId)
                .secureUrl(secureUrl)
                .resourceType(resourceType)
                .assetId(assetId)
                .publicId(publicId)
                .created(true)
                .createdAt(Instant.now())
                .build();
    }
    public static TicketPdfResponse toPdfResponse(TicketPdf ticketPdf){
        return new TicketPdfResponse(
                ticketPdf.getId(),
                ticketPdf.getTicketId(),
                ticketPdf.getSecureUrl(),
                ticketPdf.getPublicId(),
                ticketPdf.getResourceType(),
                ticketPdf.isCreated(),
                ticketPdf.getCreatedAt()
        );

    }


}
