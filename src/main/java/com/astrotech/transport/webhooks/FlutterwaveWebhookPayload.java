package com.astrotech.transport.webhooks;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FlutterwaveWebhookPayload(
        String event,
        FlutterwaveData data
) {
    public record FlutterwaveData(
            @JsonProperty("tx_ref") String txRef,
            String status,
            Long id
    ) {}
}
