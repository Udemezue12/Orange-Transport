package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record PaymentRefundRequest(
        @NotBlank(message = "Reason for refund is required")
        String reason,
        BigDecimal refundAmount
) {

}
