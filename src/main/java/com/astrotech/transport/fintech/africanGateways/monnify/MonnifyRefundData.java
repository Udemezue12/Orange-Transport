package com.astrotech.transport.fintech.africanGateways.monnify;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;


public record MonnifyRefundData(
        @JsonProperty("refundReference") String refundReference,
        @JsonProperty("paymentReference") String paymentReference,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("status") String status,
        @JsonProperty("message") String message
) {}
