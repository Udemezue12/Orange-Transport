package com.astrotech.transport.fintech.africanGateways.monnify;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonnifyInitializeData(
        @JsonProperty("transactionReference") String transactionReference,
        @JsonProperty("paymentReference") String paymentReference,
        @JsonProperty("checkoutUrl") String checkoutUrl
) {}
