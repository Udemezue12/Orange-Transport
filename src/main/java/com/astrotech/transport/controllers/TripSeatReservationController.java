package com.astrotech.transport.controllers;


import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.response.SimpleTripSeatReservationResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.TripSeatReservationWithBookingSessionResponse;
import com.astrotech.transport.dto.response.TripSeatReservationWithoutBookingSessionResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.TripSeatReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservation")
@RequiredArgsConstructor
@Tag(name = "Trip Reservations", description = "fetching reservations")
public class TripSeatReservationController {
    private final TripSeatReservationService reservationService;
    private final GetCurrentUser getCurrentUser;
    private final ApiCacheControl apiCacheControl;

    @GetMapping("/user/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripSeatReservationResponse>>> getAllUserReservations(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                                                @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();


        var response = reservationService.getAllUserReservations(userId, page, size, sortBy);
        return ApiResponseBuilder.success("Reservations fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));

    }

    @GetMapping("/all")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripSeatReservationResponse>>> getAllReservations(@RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                                            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {


        var response = reservationService.getAllReservations(page, size, sortBy);
        return ApiResponseBuilder.success("Reservations fetched Successfully", response,apiCacheControl.publicMaxAgeHours(1));

    }

    @GetMapping("/get/{userId}")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripSeatReservationResponse>>> getAllUserReservationsForAdmin(@PathVariable UUID userId, @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                                                        @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {


        var response = reservationService.getAllUserReservations(userId, page, size, sortBy);
        return ApiResponseBuilder.success("Reservations fetched Successfully", response,apiCacheControl.publicMaxAgeHours(1));

    }

    @GetMapping("/get/{tripId}/trip")
    @Ratelimit
    public ResponseEntity<ApiResponse<SliceResponse<SimpleTripSeatReservationResponse>>> getAllTripReservations(@PathVariable UUID tripId, @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
                                                                                                                @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size, @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy) {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = reservationService.getAllTripReservations(tripId, currentUserId,page, size, sortBy);
        return ApiResponseBuilder.success("Reservations fetched Successfully", response, apiCacheControl.publicMaxAgeHours(1));

    }

    @GetMapping("/{reservationId}/get-booking")
    @Ratelimit
    public ResponseEntity<ApiResponse<TripSeatReservationWithBookingSessionResponse>> getReservationWithBooking(@PathVariable UUID reservationId) {
        var response = reservationService.getReservationWithBooking(reservationId);
        return ApiResponseBuilder.success("Reservation fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{reservationId}/get")
    @Ratelimit
    public ResponseEntity<ApiResponse<TripSeatReservationWithoutBookingSessionResponse>> getReservationWithoutBooking(@PathVariable UUID reservationId) {
        var response = reservationService.getReservationWithoutBooking(reservationId);
        return ApiResponseBuilder.success("Reservation fetched Successfully", response, apiCacheControl.noStore());
    }
}
