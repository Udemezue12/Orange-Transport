package com.astrotech.transport.controllers;

import com.astrotech.transport.Idempotency.Idempotent;
import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.GetCalculatedPagination;
import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.request.PaymentRefundRequest;
import com.astrotech.transport.dto.request.PaymentRequest;
import com.astrotech.transport.dto.response.PaymentResponse;
import com.astrotech.transport.dto.response.SimplePaymentResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.fintech.data.PaymentInitializeResponse;
import com.astrotech.transport.fintech.data.PaymentRefundResponse;
import com.astrotech.transport.fintech.data.PaymentVerifyResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Endpoints for everything payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final GetCurrentUser getCurrentUser;
    private final ApiCacheControl apiCacheControl;

    @PostMapping("/{bookingCode}/initialize")
    @Ratelimit(times = 4, seconds = 8)
    @Idempotent(ttl = 50)
    public ResponseEntity<PaymentInitializeResponse> startPayment(
            @PathVariable String bookingCode,
            @Valid @RequestBody PaymentRequest request) {

        var response = paymentService.initializePayment(bookingCode, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reference}/verify")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<PaymentVerifyResponse>> verifyPayment(
            @PathVariable String reference) {

        var response = paymentService.verifyPayment(reference);
        return ApiResponseBuilder.success("Payment verified successfully", response, apiCacheControl.noStore());

    }

    @PostMapping("/{paymentId}/status/refunded")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<SimplePaymentResponse>> updatePaymentRefundCompletedStatus(@PathVariable UUID paymentId) {

        var response = paymentService.updatePaymentRefundCompletedStatus(paymentId);
        return ApiResponseBuilder.success("Payment Refunded Successfully", response, apiCacheControl.noStore());

    }

    @PostMapping("/{userId}/refund")
    @Ratelimit(times = 4, seconds = 8)
    @Idempotent(ttl = 60)
    public ResponseEntity<ApiResponse<PaymentRefundResponse>> refundPayment(
            @PathVariable UUID userId, @Valid @RequestBody PaymentRefundRequest request) {
        var response = paymentService.refundPayment(userId, request);
        return ApiResponseBuilder.success("Payment refunded successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/user/all")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<SliceResponse<SimplePaymentResponse>>> getAllUserPayments(
            @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = paymentService.getAllUserPayments(currentUserId, sortBy, page, size);
        return ApiResponseBuilder.success("Payments fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{paymentId}/get/user")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<PaymentResponse>> getUserPayment(@PathVariable UUID paymentId) {
        var currentUserId = getCurrentUser.getCurrentUserIdAndRole().userId();
        var response = paymentService.getUserPayment(paymentId, currentUserId);
        return ApiResponseBuilder.success("Payment fetched Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/{paymentId}/get/{userId}")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<PaymentResponse>> userPayment(@PathVariable UUID paymentId, @PathVariable UUID currentUserId) {

        var response = paymentService.getUserPayment(paymentId, currentUserId);
        return ApiResponseBuilder.success("Payment fetched Successfully", response, apiCacheControl.noStore());

    }

    @GetMapping("/all")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<SliceResponse<SimplePaymentResponse>>> getAllPayments(
            @RequestParam(required = false, defaultValue = "", name = "sortBy") String sortBy,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_PAGE, name = "page") int page,
            @RequestParam(required = false, defaultValue = GetCalculatedPagination.DEFAULT_SIZE, name = "size") int size) {
        var response = paymentService.getPaymentsForAdmin(sortBy, page, size);
        return ApiResponseBuilder.success("Payments fetched Successfully", response, apiCacheControl.noStore());
    }

    @GetMapping("/{paymentId}")
    @Ratelimit(times = 4, seconds = 8)
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID paymentId) {

        var response = paymentService.getPayment(paymentId);
        return ApiResponseBuilder.success("Payment fetched Successfully", response, apiCacheControl.noStore());

    }


}
