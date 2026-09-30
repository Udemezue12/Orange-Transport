package com.astrotech.transport.fintech.africanGateways.monnify;

import com.astrotech.transport.core.DateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record MonnifyVerifyData(
        @JsonProperty("transactionReference") String transactionReference,
        @JsonProperty("paymentReference") String paymentReference,
        @JsonProperty("amountPaid") BigDecimal amountPaid,
        @JsonProperty("totalPayable") BigDecimal totalPayable,
        @JsonProperty("paymentStatus") String paymentStatus,
        @JsonProperty("paymentDescription") String paymentDescription,
        @JsonProperty("currencyCode") String currencyCode,
        @JsonProperty("paymentMethod") String paymentMethod,
        @JsonProperty("paidOn")
        @JsonDeserialize(using = DateTimeDeserializer.class)
        OffsetDateTime paidOn,
        @JsonProperty("customer") Map<String, Object> customer,
        @JsonProperty("metaData") Map<String, Object> metaData
) {}
