package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.PaymentMethod;
import com.astrotech.transport.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SimplePaymentResponse(
        UUID id,
        String generatedReference,
        String paymentProviderReference,
        String paymentProviderTransactionId,
        String currency,
        String paymentChannel,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        BigDecimal amount,
        Boolean processed,
        Instant paidAt,
        Instant createdAt,
        Instant verifiedAt,
        Instant refundedAt,
        Instant failedAt
){
    
}
