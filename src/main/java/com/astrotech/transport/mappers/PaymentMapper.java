package com.astrotech.transport.mappers;

import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.dto.response.PaymentResponse;
import com.astrotech.transport.dto.response.SimplePaymentResponse;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.*;

import java.math.BigDecimal;
import java.util.List;

public class PaymentMapper {
    public static Payment createPayment(BookingSession bookingSession,String reference, PaymentMethod paymentMethod, BigDecimal amount){



        var trimmedReference = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(reference, true);
        return Payment.builder()
                .generatedReference(trimmedReference)
                .paymentMethod(paymentMethod)

                .currency(Currency.NGN)

                .amount(amount)
                .payer(bookingSession.getPassenger())
                .bookingSession(bookingSession)
                .status(PaymentStatus.PENDING)
                .build();
    }
    public static PaymentResponse mapToResponse(Payment transaction, List<Seat> seats) {
        var paymentResponse = mapToSimplePaymentResponse(transaction);
        var bookingResponse = BookingSessionMapper.mapToResponse(transaction.getBookingSession(), seats);
        var userResponse = UserMapper.response(transaction.getPayer());
        return new PaymentResponse(
                paymentResponse,
                bookingResponse,
                userResponse

        );
    }
    public static SimplePaymentResponse mapToSimplePaymentResponse(Payment transaction) {
        return new SimplePaymentResponse(
                transaction.getId(),
                transaction.getGeneratedReference(),
                transaction.getPaymentProviderReference(),
                transaction.getPaymentProviderTransactionId(),
                transaction.getCurrency().getIsoCode(),
                transaction.getPaymentChannel(),
                transaction.getPaymentMethod(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getProcessed(),
                transaction.getPaidAt(),
                transaction.getCreatedAt(),
                transaction.getVerifiedAt(),
                transaction.getRefundedAt(),
                transaction.getFailedAt()


        );

    }
}