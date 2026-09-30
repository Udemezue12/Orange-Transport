package com.astrotech.transport.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "ticket_pdfs",
        indexes = {
                @Index(name = "idx_ticket_pdf_ticket_id", columnList = "ticket_id"),
                @Index(name = "idx_ticket_pdf_status", columnList = "status"),
                @Index(name = "idx_ticket_pdf_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPdf {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ticket_id", nullable = false, unique = true)
    private UUID ticketId;


    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "public_id", unique = true)
    private String publicId;

    @Column(name = "secure_url", length = 1000)
    private String secureUrl;

    @Column(name = "created")
    private boolean created;


    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
