package com.astrotech.transport.controllers;

import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.jobrunr.enqeues.JobsEnqueue;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.TicketPdfService;
import com.astrotech.transport.service.TicketService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ticket")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "For manually creating tickets..Also for getting tickets")
public class TicketController {
    private final TicketService ticketService;
    private final TicketPdfService ticketPdfService;
    private final JobsEnqueue enqueueJobs;
    private final ApiCacheControl apiCacheControl;
    private final GetCurrentUser getCurrentUser;

    @PostMapping("/{paymentId}/generate")
    @Ratelimit
    public ResponseEntity<ApiResponse<List<TicketResponse>>> generateTickets(@PathVariable UUID paymentId) {
        var response = ticketService.generateTicket(paymentId);
        return ApiResponseBuilder.success("Tickets Generated Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{verificationToken}/verify")
    @Ratelimit
    public ResponseEntity<ApiResponse<PdfTicketResponse>> verifyTicket(@PathVariable UUID verificationToken) {
        var response = ticketService.verifyTicket(verificationToken);
        return ApiResponseBuilder.success("Tickets Verified Successfully", response, apiCacheControl.noStore());
    }

    @PostMapping("/{ticketNumber}/check-in")
    @Ratelimit
    public ResponseEntity<ApiResponse<SimpleTicketResponse>> checkIn(@PathVariable String ticketNumber) {
        var response = ticketService.checkIn(ticketNumber);
        return ApiResponseBuilder.success("Tickets Successfully Checked In", response, apiCacheControl.noStore());
    }


    @PostMapping("/{ticketId}/generate-pdf")
    @Ratelimit
    public Map<String, Object> enqueuePdf(@PathVariable UUID ticketId) {
        return enqueueJobs.enqueuePdfGeneration(ticketId);
    }

    @GetMapping("/{ticketJobId}/get-generate-status")
    @Ratelimit
    public Map<String, String> getJobStatus(UUID ticketJobId) {
        return enqueueJobs.getJobStatus(ticketJobId);

    }

    @PostMapping("/{ticketId}/download")
    @Ratelimit
    @Hidden
    public TicketPdfResponse downloadTicketPdf(UUID ticketId) {
        return ticketPdfService.downloadTicket(ticketId);

    }

    @GetMapping("/{ticketId}/pdf")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<TicketPdfResponse>>> getTicketPdfs(@PathVariable UUID ticketId,
                                                                                       @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                       @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = ticketPdfService.getTicketPdfs(ticketId, page, size);
        return ApiResponseBuilder.success("Tickets Generated Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{ticketPdfId}/pdf")
    @Ratelimit
    public ResponseEntity<ApiResponse<TicketPdfResponse>> getTicketPdf(@PathVariable UUID ticketPdfId) {
        var response = ticketPdfService.getPdfUrl(ticketPdfId);
        return ApiResponseBuilder.success("Tickets Generated Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{ticketNumber}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<TicketResponse>> getByTicketNumber(@PathVariable String ticketNumber) {
        var response = ticketService.getByTicketNumber(ticketNumber);
        return ApiResponseBuilder.success("Ticket Fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/user/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTicketResponse>>> getTicketsForUser(
            @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();

        var response = ticketService.getAllTicketsByPassenger(userId, page, size, sortBy);
        return ApiResponseBuilder.success("Tickets Fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTicketResponse>>> getAllTickets(
            @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {


        var response = ticketService.getAllTickets(page, size, sortBy);
        return ApiResponseBuilder.success("Tickets Fetched Successfully", response, apiCacheControl.noStore());
    }

}
