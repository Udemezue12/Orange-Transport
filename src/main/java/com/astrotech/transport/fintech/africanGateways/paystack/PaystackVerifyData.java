package com.astrotech.transport.fintech.africanGateways.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record PaystackVerifyData(
        @JsonProperty("id") Long id,
        @JsonProperty("domain") String domain,
        @JsonProperty("status") String status,
        @JsonProperty("reference") String reference,
        @JsonProperty("amount") Integer amount,
        @JsonProperty("gateway_response") String gatewayResponse,
        @JsonProperty("paid_at") OffsetDateTime paidAt,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("channel") String channel,
        @JsonProperty("currency") String currency,
        @JsonProperty("ip_address") String ipAddress,
        @JsonProperty("metadata") Object metadata,
        @JsonProperty("customer") Object customer
) {

}
