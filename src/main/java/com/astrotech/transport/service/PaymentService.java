package com.astrotech.transport.service;


import com.astrotech.transport.GatewaysController.PaymentGateway;
import com.astrotech.transport.core.GenerateReference;
import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.PaymentRefundRequest;
import com.astrotech.transport.dto.request.PaymentRequest;
import com.astrotech.transport.dto.response.PaymentResponse;
import com.astrotech.transport.dto.response.SimplePaymentResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.entities.Payment;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.fintech.data.PaymentInitializeResponse;
import com.astrotech.transport.fintech.data.PaymentRefundResponse;
import com.astrotech.transport.fintech.data.PaymentVerifyResponse;
import com.astrotech.transport.mappers.PaymentMapper;
import com.astrotech.transport.repositories.PaymentRepository;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentProcessingService paymentProcessingService;
    private final PaymentRepository paymentRepo;
    private final BookingSessionService sessionService;
    private final PaymentGateway paymentGateway;


    @RoleRequired({UserRole.PASSENGER, UserRole.CASHIER})
    public PaymentInitializeResponse initializePayment(String bookingReference, PaymentRequest request) {
        var bookingSession = sessionService.getBookingSession(bookingReference);

        var reference = GenerateReference.generateReference(
                request.paymentMethod().getPrefix());
        var amount = bookingSession.getTotalAmount();
        var email = bookingSession.getPassenger().getEmail();
        var payment = paymentGateway.initializeGateway(
                request.paymentMethod()
                        .name()
                        .toLowerCase(),
                reference,
                amount,
                email);

        return paymentProcessingService.getPaymentInitializeResponse(request, bookingSession, amount, payment);

    }


    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR, UserRole.PASSENGER})
    public PaymentVerifyResponse verifyPayment(String incomingReference) {
        var trimmedReference = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(incomingReference, true);
        var payment = getPaymentGeneratedReference(trimmedReference);
        var transactionReference = payment.getGeneratedReference();
        if (!transactionReference.equals(incomingReference)) {
            log.error("CRITICAL MISMATCH: Internal Ref: {} vs Redirect Ref: {}",
                    incomingReference, transactionReference);
            throw new BadRequestException("CRITICAL REFERENCE MISMATCH");
        }


        checkPayment(payment);
        var response = paymentGateway.verifyGateway(payment.getGeneratedReference(),
                payment.getPaymentMethod().name().toLowerCase());
        var sessionId = payment.getBookingSession().getId();
        var sessionCode = payment.getBookingSession().getReference();


        var finalReference = response.gatewayReference() != null
                ? response.gatewayReference()
                : payment.getGeneratedReference();
        var verified = response.success();
        paymentProcessingService.updatePaymentTransaction(
                payment,
                verified,
                finalReference,
                response.transactionId(),
                response.currency(),
                response.channel(),
                sessionId,
                sessionCode
        );

        return verified
                ? response
                : PaymentVerifyResponse.builder()
                .success(false)
                .status(PaymentStatus.FAILED.name())
                .build();
    }

    public void webhookVerifyPayment(String incomingReference) {
        var trimmedReference = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(incomingReference, true);
        var payment = getPaymentGeneratedReference(trimmedReference);


        var transactionReference = payment.getGeneratedReference();
        if (!transactionReference.equals(incomingReference)) {
            log.error("CRITICAL MISMATCH: Internal Ref: {} vs Webhook Ref: {}",
                    incomingReference, transactionReference);
            PaymentVerifyResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED.name())
                    .build();
            return;
        }


        checkPayment(payment);
        var response = paymentGateway.verifyWebhookGateway(payment.getGeneratedReference(),
                payment.getPaymentMethod().name().toLowerCase());
        var sessionId = payment.getBookingSession().getId();
        var sessionCode = payment.getBookingSession().getReference();


        var finalReference = response.gatewayReference() != null
                ? response.gatewayReference()
                : payment.getGeneratedReference();
        var verified = response.success();
        paymentProcessingService.updatePaymentTransaction(
                payment,
                verified,
                finalReference,
                response.transactionId(),
                response.currency(),
                response.channel(),
                sessionId,
                sessionCode
        );

        if (!verified) {
            PaymentVerifyResponse.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED.name())
                    .build();
        }


    }


    @RoleRequired(UserRole.ADMIN)
    public PaymentRefundResponse refundPayment(
            UUID userId,
            PaymentRefundRequest request
    ) {
        var payment = getPaymentPayer(userId);

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return PaymentRefundResponse.builder()
                    .success(false)
                    .status("ALREADY_REFUNDED")
                    .message("Payment has already been refunded")
                    .reference(payment.getGeneratedReference())
                    .build();
        }

        if (payment.getStatus() == PaymentStatus.PROCESSING_REFUND) {
            return PaymentRefundResponse.builder()
                    .success(false)
                    .status("REFUND_ALREADY_PROCESSING")
                    .message("Payment refund is already being processed")
                    .reference(payment.getGeneratedReference())
                    .build();
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            return PaymentRefundResponse.builder()
                    .success(false)
                    .status("INVALID_STATUS")
                    .message("Only successful payments can be refunded")
                    .reference(payment.getGeneratedReference())
                    .build();
        }

        var finalAmount = request.refundAmount() != null
                ? request.refundAmount()
                : payment.getAmount();

        var gateway = paymentGateway.refundGateway(
                payment.getPaymentProviderTransactionId(),
                payment.getPaymentMethod().name().toLowerCase(),
                finalAmount,
                request.reason()
        );

        if (!gateway.success()) {
            log.warn(
                    "Refund request failed. Reference={}, Gateway={}, Message={}",
                    payment.getPaymentProviderReference(),
                    payment.getPaymentMethod(),
                    gateway.message()
            );

            return PaymentRefundResponse.builder()
                    .success(false)
                    .status("REFUND_FAILED")
                    .message(gateway.message())
                    .reference(payment.getGeneratedReference())
                    .build();
        }
        var amount = gateway.amount() != null
                ? gateway.amount()
                : finalAmount;
        var response = PaymentRefundResponse.builder()
                .success(true)
                .status(PaymentStatus.PROCESSING_REFUND.name())
                .message("Refund request submitted successfully and is being processed")
                .refundId(gateway.refundId())
                .reference(payment.getPaymentProviderReference())
                .amount(amount)
                .currency(payment.getCurrency().getIsoCode())
                .build();
        paymentProcessingService.saveRefundProcessing(payment, response.refundId());
        return response;


    }


    @RoleRequired(UserRole.ADMIN)
    public SimplePaymentResponse updatePaymentRefundCompletedStatus(
            UUID paymentId
    ) {
        var payment = findById(paymentId);

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return PaymentMapper.mapToSimplePaymentResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.PROCESSING_REFUND) {
            throw new BadRequestException(
                    "Payment is not currently being refunded"
            );
        }
        var Id = payment.getRefundId() != null
                ? payment.getRefundId()
                : payment.getPaymentProviderTransactionId();

        var refund = paymentGateway.verifyRefundGateway(
                Id,
                payment.getPaymentMethod().name().toLowerCase()
        );

        if (!"processed".equalsIgnoreCase(refund.status())
                && !"completed".equalsIgnoreCase(refund.status())
                && !"success".equalsIgnoreCase(refund.status())) {

            throw new BadRequestException(
                    "Refund has not been completed. Current status: "
                            + refund.status()
            );
        }

        var savedPayment = paymentProcessingService.saveRefundCompleted(
                payment,
                refund
        );

        return PaymentMapper.mapToSimplePaymentResponse(savedPayment);
    }

// //////////////////////////////////////////////////////////
//    PAYMENT BUSINESS LOGIC
// /////////////////////////////////////////////////////////
// ////////////////////////////////////////////////////////


    public Payment findById(UUID paymentId) {
        return paymentRepo.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Payment missing for ID: " + paymentId));
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "userPayments",
            key = "#userId + '-p' + #page + '-s' + #size + '-sort-' + #sortBy",
            ttl = 120,
            timeUnit = TimeUnit.SECONDS
    )
    @RoleRequired({UserRole.PASSENGER, UserRole.ADMIN})
    public SliceResponse<SimplePaymentResponse> getAllUserPayments(UUID userId,
                                                                   String sortBy, int page, int size) {

        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Payment.class, true);

        var result = paymentRepo
                .findAllByPayerId(userId, pageable);
        var content = result.getContent()
                .stream()
                .map(PaymentMapper::mapToSimplePaymentResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "userPayments",
            key = "#userId + '-' + #paymentId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    @RoleRequired({UserRole.PASSENGER, UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    public PaymentResponse getUserPayment(UUID paymentId, UUID userId) {
        var payment = paymentRepo.findByIdAndPayerId(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("No payments found"));
        var seats = sessionService.getSeatList(payment.getBookingSession().getId());
        return PaymentMapper.mapToResponse(payment, seats);

    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "adminPayments",
            key = "'page-' + #page + '-size-' + #size + '-sort-' + #sortBy",
            ttl = 60,
            timeUnit = TimeUnit.SECONDS
    )
    @RoleRequired(UserRole.ADMIN)
    public SliceResponse<SimplePaymentResponse> getPaymentsForAdmin(
            String sortBy, int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, Payment.class, true);

        var result = paymentRepo
                .findAllBy(pageable);
        var content = result.getContent()
                .stream()
                .map(PaymentMapper::mapToSimplePaymentResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "adminPayment",
            key = "'admin-' + #paymentId",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public PaymentResponse getPayment(
            UUID paymentId) {

        var payment = findById(paymentId);
        var seats = sessionService.getSeatList(payment.getBookingSession().getId());
        return PaymentMapper.mapToResponse(payment, seats);
    }


    public Payment getPaymentGeneratedReference(String generatedReference) {
        return paymentRepo.findByGeneratedReferenceWithPayer(generatedReference)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }


    private @NonNull Payment getPaymentPayer(UUID userId) {
        return paymentRepo.findByPayerId(userId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    private void checkPayment(Payment payment) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            throw new BadRequestException(
                    "Payment already verified");
        }

        if (Boolean.TRUE.equals(payment.getProcessed())) {

            throw new BadRequestException(
                    "Booking already processed for this transaction");
        }
    }


}
