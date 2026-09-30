package com.astrotech.transport.fintech.africanGateways.flutterwave;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FlutterwaveVerifyData(
        @JsonProperty("id") Long id,
        @JsonProperty("tx_ref") String txRef,
        @JsonProperty("flw_ref") String flwRef,
        @JsonProperty("device_fingerprint") String deviceFingerprint,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("charged_amount") BigDecimal chargedAmount,
        @JsonProperty("app_fee") BigDecimal appFee,
        @JsonProperty("merchant_fee") BigDecimal merchantFee,
        @JsonProperty("processor_response") String processorResponse,
        @JsonProperty("auth_model") String authModel,
        @JsonProperty("ip") String ip,
        @JsonProperty("narration") String narration,
        @JsonProperty("status") String status,
        @JsonProperty("payment_type") String paymentType,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("account_id") Long accountId,
        @JsonProperty("meta") Object meta,
        @JsonProperty("customer") Object customer
) {
}