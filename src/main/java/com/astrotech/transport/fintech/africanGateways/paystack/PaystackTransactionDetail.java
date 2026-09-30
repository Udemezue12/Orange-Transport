package com.astrotech.transport.fintech.africanGateways.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackTransactionDetail(
        @JsonProperty("id") Long id,
        @JsonProperty("reference") String reference,
        @JsonProperty("status") String status,
        @JsonProperty("amount") Integer amount,
        @JsonProperty("currency") String currency
) {
}
