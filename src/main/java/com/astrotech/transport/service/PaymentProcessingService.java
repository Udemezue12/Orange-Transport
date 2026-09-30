package com.astrotech.transport.service;


import com.astrotech.transport.dto.request.PaymentRequest;
import com.astrotech.transport.entities.BookingSession;
import com.astrotech.transport.entities.Payment;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.events.GenerateTicketEvent;
import com.astrotech.transport.events.PaymentSuccessEvent;
import com.astrotech.transport.fintech.data.PaymentInitializeResponse;
import com.astrotech.transport.fintech.data.PaymentRefundResponse;
import com.astrotech.transport.mappers.PaymentMapper;
import com.astrotech.transport.notifications.WebsocketNotifications;
import com.astrotech.transport.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentRepository paymentRepo;
    private final BookingSessionService sessionService;
    private final WebsocketNotifications notificationService;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userPayments", allEntries = true),
            @CacheEvict(value = "adminPayments", allEntries = true)
    })
    public PaymentInitializeResponse getPaymentInitializeResponse(PaymentRequest request, BookingSession bookingSession, BigDecimal amount, PaymentInitializeResponse payment) {
        var paymentMapper = PaymentMapper.createPayment(bookingSession, payment.reference(), request.paymentMethod(), amount);
        sessionService.updateSessionStatus(bookingSession.getId(), BookingSessionStatus.PAYMENT_PENDING);
        paymentRepo.save(paymentMapper);

        notificationService.sendNotifications(
                bookingSession.getPassenger(),
                NotificationType.PAYMENT_PENDING,
                "Payment Started",
                "Payment was successfully created for this booking",
                "/booking/" + bookingSession.getReference(),
                bookingSession.getId(),
                NotificationReferenceType.BOOKING_SESSION
        );


        return payment;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userPayments", allEntries = true),
            @CacheEvict(value = "adminPayments", allEntries = true),
            @CacheEvict(value = "adminPayment", allEntries = true)
    })
    public void updatePaymentTransaction(
            Payment payment,
            boolean verified,
            String reference,
            String transactionId,
            String currency,
            String channel,
            UUID bookingId,
            String bookingCode
    ) {

        if (verified) {
            var paymentCurrency = Currency.fromIsoCode(currency);


            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setVerifiedAt(Instant.now());
            payment.setPaidAt(Instant.now());
            payment.setPaymentProviderReference(reference);
            payment.setPaymentProviderTransactionId(transactionId);
            payment.setProcessed(true);
            payment.setCurrency(paymentCurrency);
            payment.setPaymentChannel(channel);


            var savedPayment = paymentRepo.save(payment);
            var booked = sessionService.updateSessionStatus(bookingId, BookingSessionStatus.PAYMENT_PROCESSED);

            if (savedPayment.getStatus() == PaymentStatus.SUCCESS && booked.status() == BookingSessionStatus.PAYMENT_PROCESSED) {
                applicationEventPublisher.publishEvent(getPaymentSuccessEventPublisher(payment, bookingCode));
                applicationEventPublisher.publishEvent(generateTicketEventPublisher(payment.getId()));
            }
            notificationService.sendNotifications(
                    payment.getPayer(),
                    NotificationType.PAYMENT_SUCCESS,
                    "Payment Successful",
                    "Your payment was successful and your booking has been confirmed.",
                    "/booking/" + bookingCode,
                    bookingId,
                    NotificationReferenceType.BOOKING_SESSION
            );

            return;
        }


        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailedAt(Instant.now());
        sessionService.updateSessionStatus(bookingId, BookingSessionStatus.PAYMENT_PENDING);

        paymentRepo.save(payment);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userPayments", allEntries = true),
            @CacheEvict(value = "adminPayments", allEntries = true),
            @CacheEvict(value = "adminPayment", allEntries = true)
    })
    public void saveRefundProcessing(Payment payment, String refundId) {

        payment.setStatus(PaymentStatus.PROCESSING_REFUND);
        if (refundId != null) {
            payment.setRefundId(refundId);
        }

        sessionService.updateSessionStatus(
                payment.getBookingSession().getId(),
                BookingSessionStatus.CANCELLED
        );

        paymentRepo.save(payment);

        notificationService.sendNotifications(
                payment.getPayer(),
                NotificationType.PAYMENT_REFUND_PROCESSING,
                "Payment Refund Processing",
                "Your payment refund has been initiated and is being processed.",
                "/payment/" + payment.getId(),
                payment.getId(),
                NotificationReferenceType.PAYMENT
        );

    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userPayments", allEntries = true),
            @CacheEvict(value = "adminPayments", allEntries = true),
            @CacheEvict(value = "adminPayment", allEntries = true)
    })
    public Payment saveRefundCompleted(
            Payment payment,
            PaymentRefundResponse refund
    ) {
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(Instant.now());


        if (refund.amount() != null) {
            payment.setRefundedAmount(refund.amount());
        }


        var savedPayment = paymentRepo.save(payment);

        notificationService.sendNotifications(
                payment.getPayer(),
                NotificationType.PAYMENT_REFUNDED,
                "Payment Successfully Refunded",
                "Your payment refund has been completed successfully.",
                "/payment/" + payment.getId(),
                payment.getId(),
                NotificationReferenceType.PAYMENT
        );

        return savedPayment;
    }

    private PaymentSuccessEvent getPaymentSuccessEventPublisher(Payment transaction, String bookingCode) {
        return new PaymentSuccessEvent(
                bookingCode,
                transaction.getPayer().getFullName(),
                transaction.getPayer().getEmail(),
                transaction.getId(),
                transaction.getPayer().getPhoneNumber());
    }

    private GenerateTicketEvent generateTicketEventPublisher(UUID paymentId) {
        return new GenerateTicketEvent(
                paymentId
        );
    }
}
