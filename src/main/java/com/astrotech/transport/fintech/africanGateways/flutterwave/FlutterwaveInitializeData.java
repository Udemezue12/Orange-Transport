package com.astrotech.transport.fintech.africanGateways.flutterwave;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FlutterwaveInitializeData(
        @JsonProperty("link") String link
) {
}
