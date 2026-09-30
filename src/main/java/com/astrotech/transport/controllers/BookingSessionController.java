package com.astrotech.transport.controllers;

import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.request.CreateBookingSessionRequest;
import com.astrotech.transport.dto.response.BookingSessionResponse;
import com.astrotech.transport.dto.response.SimpleBookingSessionResponse;
import com.astrotech.transport.dto.response.SimpleBookingSessionWithoutSeatsResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.BookingSessionService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "For bookings")
public class BookingSessionController {
    private final BookingSessionService bookingSessionService;
    private final GetCurrentUser getCurrentUser;
    private final ApiCacheControl apiCacheControl;


    @PostMapping("/create")
    @Ratelimit
    public ResponseEntity<ApiResponse<SimpleBookingSessionResponse>> createBooking(@Valid @RequestBody CreateBookingSessionRequest request) {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = bookingSessionService.createSession(request, userId);
        return ApiResponseBuilder.success("Booked Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/{bookingCode}/get-code")
    @Ratelimit
    public ResponseEntity<ApiResponse<BookingSessionResponse>> getBooking(@PathVariable String bookingCode) {

        var response = bookingSessionService.getBooking(bookingCode);
        return ApiResponseBuilder.success("Fetched Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/{bookingId}")
    @Hidden
    @Ratelimit
    public ResponseEntity<ApiResponse<BookingSessionResponse>> getBookingById(@PathVariable UUID bookingId) {

        var response = bookingSessionService.getBookingById(bookingId);
        return ApiResponseBuilder.success("Fetched Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/{bookingCode}/user")
    @Ratelimit
    public ResponseEntity<ApiResponse<BookingSessionResponse>> getUserBooking(@PathVariable String bookingCode) {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = bookingSessionService.getUserBooking(bookingCode, userId);
        return ApiResponseBuilder.success("Fetched Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/user")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleBookingSessionWithoutSeatsResponse>>> getUserBookings(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                                                @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = bookingSessionService.getUserBookings(userId, page, size);
        return ApiResponseBuilder.success("Fetched Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleBookingSessionWithoutSeatsResponse>>> getBookings(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                                            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {

        var response = bookingSessionService.getAllBookings(page, size);
        return ApiResponseBuilder.success("Fetched Successfully", response, apiCacheControl.noStore());

    }

}
