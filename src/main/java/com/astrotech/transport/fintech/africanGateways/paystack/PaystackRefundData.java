package com.astrotech.transport.fintech.africanGateways.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record PaystackRefundData(
        @JsonProperty("id") Long id,
        @JsonProperty("success") Boolean success,
        @JsonProperty("transaction") PaystackTransactionDetail transaction,
        @JsonProperty("integration") Long integration,
        @JsonProperty("deducted_amount") Integer deductedAmount,
        @JsonProperty("fully_refunded_at") OffsetDateTime fullyRefundedAt,
        @JsonProperty("customer_note") String customerNote,
        @JsonProperty("merchant_note") String merchantNote,
        @JsonProperty("status") String status,
        @JsonProperty("refunded_by") String refundedBy,
        @JsonProperty("expected_at") OffsetDateTime expectedAt,
        @JsonProperty("currency") String currency,
        @JsonProperty("amount") Integer amount,
        @JsonProperty("transaction_reference") String transactionReference,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt
) {
}


