package com.astrotech.transport.service;

import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TicketPdfResponse;
import com.astrotech.transport.entities.TicketPdf;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.exceptions.TicketGenerationPendingException;
import com.astrotech.transport.jobrunr.enqeues.JobsEnqueue;
import com.astrotech.transport.mappers.TicketPdfMapper;
import com.astrotech.transport.repositories.TicketPdfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TicketPdfService {
    private final TicketPdfRepository ticketPdfRepository;
    private final JobsEnqueue jobsEnqueue;


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ticket-pdfs", key = "'pdf-' + #ticketId"),
            @CacheEvict(value = "ticket-pdfs", key = "'download-' + #ticketId"),
            @CacheEvict(value = "ticket-pdfs", allEntries = true)
    })
    public TicketPdfResponse savePdfMetadata(UUID ticketId, String assetId, String resourceType, String secureUrl, String publicId) {

        var ticketPdfMapper = TicketPdfMapper.createPdf(ticketId, assetId, resourceType, secureUrl, publicId);
        var savedPdf = ticketPdfRepository.save(ticketPdfMapper);


        return TicketPdfMapper.toPdfResponse(savedPdf);
    }


    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "ticket-pdfs",
            key = "'meta-' + #ticketPdfId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public TicketPdfResponse getPdfUrl(UUID ticketPdfId) {
        return ticketPdfRepository.findById(ticketPdfId)
                .map(TicketPdfMapper::toPdfResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket PDF not found with id: " + ticketPdfId));
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "ticket-pdfs",
            key = "'list-' + #ticketId + '-p' + #page + '-s' + #size",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<TicketPdfResponse> getTicketPdfs(UUID ticketId, int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "createdAt", true, TicketPdf.class, true);
        var result = ticketPdfRepository.findAllByTicketId(ticketId, pageable);
        var content = result.getContent()
                .stream()
                .map(TicketPdfMapper::toPdfResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }


    public List<TicketPdf> getTicketPdfCreatedOn(Instant cutoff) {
        return ticketPdfRepository.findByCreatedAtBefore(cutoff);
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "ticket-pdfs",
            key = "'download-' + #ticketId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public TicketPdfResponse downloadTicket(UUID ticketId) {

        return ticketPdfRepository.findByTicketId(ticketId)
                .map(TicketPdfMapper::toPdfResponse)
                .orElseGet(() -> {
                    jobsEnqueue.enqueuePdfGeneration(ticketId);
                    throw new TicketGenerationPendingException(ticketId);
                });

    }



    @Transactional
    @CacheEvict(value = "ticket-pdfs", allEntries = true)
    public void deleteAll(List<TicketPdf> expiredPdfs) {
        ticketPdfRepository.deleteAllInBatch(expiredPdfs);
    }

}
