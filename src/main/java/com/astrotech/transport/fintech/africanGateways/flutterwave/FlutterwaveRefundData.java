package com.astrotech.transport.fintech.africanGateways.flutterwave;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FlutterwaveRefundData(
        @JsonProperty("id") String id,
        @JsonProperty("success") Boolean success,
        @JsonProperty("account_id") Long accountId,
        @JsonProperty("tx_id") Long txId,
        @JsonProperty("flw_ref") String flwRef,
        @JsonProperty("wallet_id") Long walletId,
        @JsonProperty("amount_refunded") BigDecimal amountRefunded,
        @JsonProperty("status") String status,
        @JsonProperty("destination") String destination,
        @JsonProperty("meta") Object meta,
        // Replace with specific record structure if you pass custom meta object array
        @JsonProperty("created_at") OffsetDateTime createdAt
) {
}