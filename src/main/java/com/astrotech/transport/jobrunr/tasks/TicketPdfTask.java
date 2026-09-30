package com.astrotech.transport.jobrunr.tasks;


import com.astrotech.transport.cloudinary.CloudinaryService;
import com.astrotech.transport.entities.TicketPdf;
import com.astrotech.transport.enums.NotificationReferenceType;
import com.astrotech.transport.enums.NotificationType;
import com.astrotech.transport.notifications.WebsocketNotifications;
import com.astrotech.transport.service.TicketPdfService;
import com.astrotech.transport.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketPdfTask {
    private final TicketPdfService ticketPdfService;
    private final CloudinaryService cloudinaryService;
    private final TicketService ticketService;
    private final WebsocketNotifications notificationService;

    @Job(name = "Generate Ticket PDF and Upload for Ticket %0")
    public void generateAndSavePdfJob(UUID ticketId) {
        log.info("Starting PDF generation and upload job for ticket ID: {}", ticketId);

        var ticket = ticketService.generateAndConvertTicketPdf(ticketId);

        var upload = cloudinaryService.uploadPdf(
                ticket.bytes(),
                "transport/tickets",
                ticket.ticketNumber() + ".pdf"
        );

        var pdfSaved = ticketPdfService.savePdfMetadata(
                ticket.ticketId(),
                upload.assetId(),
                upload.resourceType(),
                upload.secureUrl(),
                upload.publicId()
        );
        if (pdfSaved.created()) {
            var user = ticket.user();

            notificationService.sendNotifications(
                    user,
                    NotificationType.TICKET_GENERATED,
                    "Ticket Successfully Generated",
                    "Ticket successfully generated in pdf format",
                    upload.secureUrl(),
                    pdfSaved.Id(),
                    NotificationReferenceType.TICKET
            );
        }


        log.info(
                "PDF saved successfully. publicId={}, ticketId={}",
                pdfSaved.publicId(),
                pdfSaved.ticketId()
        );

    }

    @Job(name = "Purge Expired Ticket PDFs (older than 2 days)")

    public void purgeExpiredTicketPdfsJob() {

        var cutoff = Instant.now().minus(2, ChronoUnit.DAYS);
        var expiredPdfs = ticketPdfService.getTicketPdfCreatedOn(cutoff);

        if (expiredPdfs.isEmpty()) {
            log.info("No expired ticket PDFs found for purge.");
            return;
        }

        log.info("Found {} expired ticket PDFs to purge.", expiredPdfs.size());
        var publicIds = expiredPdfs
                .stream()
                .map(TicketPdf::getPublicId)
                .toList();


        var result = cloudinaryService.deleteResources(publicIds);
        if (result) {
            ticketPdfService.deleteAll(expiredPdfs);
            log.info("Successfully purged {} records from database.", expiredPdfs.size());
        }

    }

}