package com.astrotech.transport.fintech.africanGateways.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackInitializeData(
        @JsonProperty("authorization_url") String authorizationUrl,
        @JsonProperty("access_code") String accessCode,
        @JsonProperty("reference") String reference
) {
}
